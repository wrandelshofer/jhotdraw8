/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champset.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.champset.Node;
import org.jhotdraw8.icollection.impl.linked.LinkedElement;
import org.jhotdraw8.icollection.impl.linked.LinkedElementIterator;
import org.jhotdraw8.icollection.impl.linked.ReversedLinkedElementIterator;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;


/// Implements the [PersistentSequencedSet] interface using a Compressed Hash-Array
/// Mapped Prefix-tree (CHAMP) and a doubly linked list.
///
/// Features:
///
///   - supports up to 2<sup>32</sup> - 1 elements
///   - does not allow null elements
///   - is persistent
///   - is thread-safe
///   - guarantees iteration order
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N)
///   - remove: O(log₃₂ N
///   - contains: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy
///   - clone: O(1)
///   - iterator.next(): O(log₃₂ N)
///
/// @param <E> the element type
@SuppressWarnings("exports")
public class PersistentLinkedHashElementSet<E> implements PersistentSequencedSet<E>, Serializable {
    private final @Nullable LinkedElement<E> first;
    private final @Nullable LinkedElement<E> last;

    @Override
    public ReadableSequencedSet<E> readableReversed() {
        return new ReadableSequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.IMMUTABLE);
    }


    private static final PersistentLinkedHashElementSet<?> EMPTY = new PersistentLinkedHashElementSet<>(null, null, BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode<LinkedElement<E>> hashSet;
    final int size;

    PersistentLinkedHashElementSet(@Nullable LinkedElement<E> first, @Nullable LinkedElement<E> last, BitmapIndexedNode<LinkedElement<E>> root, int size) {
        this.first = first;
        this.last = last;
        this.hashSet = root;
        this.size = size;
    }


    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param c   an iterable
    /// @param <E> the element type
    /// @return a persistent set of the provided elements
    public static <E> PersistentLinkedHashElementSet<E> copyOf(Iterable<? extends E> c) {
        return PersistentLinkedHashElementSet.<E>of().addingAll(c);
    }

    /// Returns an empty persistent set.
    ///
    /// @param <E> the element type
    /// @return an empty persistent set
    @SuppressWarnings("unchecked")
    public static <E> PersistentLinkedHashElementSet<E> of() {
        return ((PersistentLinkedHashElementSet<E>) PersistentLinkedHashElementSet.EMPTY);
    }

    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param elements elements
    /// @param <E>      the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> PersistentLinkedHashElementSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new PersistentLinkedHashElementSetBuilder<E>().addArray(elements).build();
    }

    /// Update function for a set: we always keep the old element.
    ///
    /// @param oldElement the old element
    /// @param newElement the new element
    /// @param <E>        the element type
    /// @return always returns the old element
    static <E> E updateElement(E oldElement, E newElement) {
        return oldElement;
    }

    public static <E> E insertOrFail(E oldElement, E newElement) {
        throw new IllegalArgumentException("Element is already in the set. elem=" + oldElement);
    }

    static int keyHash(Object e) {
        return Objects.hashCode(e);
    }

    @Override
    public PersistentLinkedHashElementSet<E> adding(@Nullable E element) {
        return addLast(element, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentLinkedHashElementSet<E> addingAll(Iterable<? extends E> c) {
        if (isEmpty() && c instanceof PersistentLinkedHashElementSet<? extends E> s) {
            return (PersistentLinkedHashElementSet<E>) s;
        }
        var m = toMutable();
        return m.addAll(c) ? m.toPersistent() : this;
    }

    private static <E> @Nullable LinkedElement<E> get(BitmapIndexedNode<LinkedElement<E>> hashSet, @Nullable E o) {
        if (o == null) {
            return null;
        }
        Object result = hashSet.find(
                new LinkedElement<>((E) o, null, null),
                Objects.hashCode(o), 0, Objects::equals);
        return result == org.jhotdraw8.icollection.impl.champset.Node.NO_DATA ? null : (LinkedElement<E>) result;
    }

    @Override
    public PersistentLinkedHashElementSet<E> addingFirst(@Nullable E element) {
        return addFirst(element, true);
    }

    private PersistentLinkedHashElementSet<E> addFirst(@Nullable E e, boolean moveToFirst) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, null, first == null ? null : first.key());
        var newHashSet = this.hashSet.put(null,
                linkedElement, PersistentLinkedHashElementSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToFirst && oldKey.prev() != null ? newKey : oldKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        if (details.isModified()) {
            Updated<E> updated = new Updated<>(newHashSet, first, last);
            if (details.isReplaced()) {
                var removed = details.getOldData();
                assert removed != null;
                updated = updateNext(updated.hashSet, get(updated.hashSet, removed.prev()), removed.next(), updated.first, updated.last);
                updated = updatePrev(updated.hashSet, get(updated.hashSet, removed.next()), removed.prev(), updated.first, updated.last);
                updated = updatePrev(updated.hashSet, updated.first, e, updated.first, updated.last);
                if (removed == updated.last) {
                    updated = new Updated<>(updated.hashSet, updated.first, get(updated.hashSet, removed.prev()));
                    ;
                }
            } else {
                if (updated.first == null) {
                    updated = new Updated<>(updated.hashSet, linkedElement, linkedElement);
                } else {
                    updated = updatePrev(updated.hashSet, updated.first, e, updated.first, updated.last);
                }
                return new PersistentLinkedHashElementSet<>(linkedElement, updated.last, updated.hashSet, size + 1);
            }
            return new PersistentLinkedHashElementSet<>(linkedElement, updated.last, updated.hashSet, size);
        }
        return this;
    }

    @Override
    public PersistentLinkedHashElementSet<E> addingLast(@Nullable E element) {
        return addLast(element, true);
    }

    private PersistentLinkedHashElementSet<E> addLast(@Nullable E e, boolean moveToLast) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, last == null ? null : last.key(), null);
        var newHashSet = hashSet.put(null,
                linkedElement, PersistentLinkedHashElementSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToLast && oldKey.next() != null ? newKey : oldKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        if (details.isModified()) {
            Updated<E> updated = new Updated<>(newHashSet, first, last);
            if (details.isReplaced()) {
                var removed = details.getOldData();
                updated = updateNext(updated.hashSet, get(updated.hashSet, removed.prev()), removed.next(), updated.first, updated.last);
                updated = updatePrev(updated.hashSet, get(updated.hashSet, removed.next()), removed.prev(), updated.first, updated.last);
                updated = updateNext(updated.hashSet, updated.last, e, updated.first, updated.last);
                if (removed == updated.first) {
                    updated = new Updated<>(updated.hashSet, get(updated.hashSet, updated.first.next()), updated.last);
                }
            } else {
                if (updated.last == null) {
                    updated = new Updated<>(updated.hashSet, linkedElement, linkedElement);
                } else {
                    updated = updateNext(updated.hashSet, updated.last, e, updated.first, updated.last);
                }
                return new PersistentLinkedHashElementSet<>(updated.first, linkedElement, updated.hashSet, size + 1);
            }
            return new PersistentLinkedHashElementSet<>(updated.first, linkedElement, updated.hashSet, size);
        }
        return this;
    }

    record Updated<E>(BitmapIndexedNode<LinkedElement<E>> hashSet, @Nullable LinkedElement<E> first,
                      @Nullable LinkedElement<E> last) {
    }

    private Updated<E> updateNext(BitmapIndexedNode<LinkedElement<E>> hashSet, @Nullable LinkedElement<E> elem, @Nullable E e, @Nullable LinkedElement<E> first, @Nullable LinkedElement<E> last) {
        if (elem == null) {
            return new Updated<>(hashSet, first, last);
        }

        LinkedElement<E> newData = new LinkedElement<>(elem.key(), elem.prev(), e);
        var ee = newData.key();
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        hashSet = hashSet.put(null,
                newData, PersistentLinkedHashElementSet.keyHash(ee), 0, details,
                (oldKey, newKey) -> newKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        var oldData = details.getOldData();

        if (last == oldData) {
            last = newData;
        }
        if (first == oldData) {
            first = newData;
        }
        return new Updated<>(hashSet, first, last);
    }

    private Updated<E> updatePrev(BitmapIndexedNode<LinkedElement<E>> hashSet, @Nullable LinkedElement<E> elem, @Nullable E e, @Nullable LinkedElement<E> first, @Nullable LinkedElement<E> last) {
        if (elem == null) {
            return new Updated<>(hashSet, first, last);
        }

        LinkedElement<E> newData = new LinkedElement<>(elem.key(), e, elem.next());
        var ee = newData.key();
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        hashSet = hashSet.put(null,
                newData, PersistentLinkedHashElementSet.keyHash(ee), 0, details,
                (oldKey, newKey) -> newKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        var oldData = details.getOldData();
        if (last == oldData) {
            last = newData;
        }
        if (first == oldData) {
            first = newData;
        }
        return new Updated<>(hashSet, first, last);
    }

    /// {@inheritDoc}
    @Override
    public PersistentLinkedHashElementSet<E> cleared() {
        return of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return hashSet.find(new LinkedElement<>((E) o, null, null), keyHash(o), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof PersistentLinkedHashElementSet<?> that) {
            return size == that.size && hashSet.equivalent(that.hashSet);
        }
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedElementIterator<>(first, hashSet, LinkedElement::key);
    }

    private Iterator<E> reverseIterator() {
        return new ReversedLinkedElementIterator<>(last, hashSet, LinkedElement::key);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public PersistentLinkedHashElementSet<E> removingFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removing(getFirst());
    }

    @Override
    public PersistentLinkedHashElementSet<E> removingLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removing(getLast());
    }

    @Override
    public PersistentLinkedHashElementSet<E> removing(E key) {
        int keyHash = keyHash(key);
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<LinkedElement<E>> newHashSet = hashSet.remove(null,
                new LinkedElement<>(key, null, null), keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            if (size == 1) return PersistentLinkedHashElementSet.of();
            var removed = details.getOldData();
            assert removed != null;
            Updated<E> updated = new Updated<>(newHashSet, first, last);
            updated = updateNext(updated.hashSet, get(updated.hashSet, removed.prev()), removed.next(), updated.first, updated.last);
            updated = updatePrev(updated.hashSet, get(updated.hashSet, removed.next()), removed.prev(), updated.first, updated.last);
            if (updated.last == removed)
                updated = new Updated<>(updated.hashSet, updated.first, get(updated.hashSet, updated.last.prev()));
            if (updated.first == removed)
                updated = new Updated<>(updated.hashSet, get(updated.hashSet, updated.first.next()), updated.last);
            return new PersistentLinkedHashElementSet<>(updated.first, updated.last, updated.hashSet, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashElementSet<E> removingAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashElementSet<E> retainingAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<E> spliterator() {
        return new ChampSpliterator<>(hashSet, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.NONNULL);
    }

    @Override
    public MutableLinkedHashElementSet<E> toMutable() {
        return new MutableLinkedHashElementSet<>(this);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return PersistentLinkedHashElementSet.copyOf(deserializedElements);
        }
    }
}