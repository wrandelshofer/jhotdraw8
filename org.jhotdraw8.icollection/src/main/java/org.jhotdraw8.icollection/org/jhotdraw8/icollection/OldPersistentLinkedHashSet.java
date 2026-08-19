/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChampSpliterator;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.alt.impl.champset.Node;
import org.jhotdraw8.icollection.alt.impl.linked.LinkedElement;
import org.jhotdraw8.icollection.alt.impl.linked.LinkedElementIterator;
import org.jhotdraw8.icollection.alt.impl.linked.ReversedLinkedElementIterator;
import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
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
public class OldPersistentLinkedHashSet<E> implements PersistentSequencedSet<E>, Serializable {
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


    private static final OldPersistentLinkedHashSet<?> EMPTY = new OldPersistentLinkedHashSet<>(null, null, BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode<LinkedElement<E>> root;
    final int size;

    OldPersistentLinkedHashSet(@Nullable LinkedElement<E> first, @Nullable LinkedElement<E> last, BitmapIndexedNode<LinkedElement<E>> root, int size) {
        this.first = first;
        this.last = last;
        this.root = root;
        this.size = size;
    }


    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param c   an iterable
    /// @param <E> the element type
    /// @return a persistent set of the provided elements
    public static <E> OldPersistentLinkedHashSet<E> copyOf(Iterable<? extends E> c) {
        return OldPersistentLinkedHashSet.<E>of().addingAll(c);
    }

    /// Returns an empty persistent set.
    ///
    /// @param <E> the element type
    /// @return an empty persistent set
    @SuppressWarnings("unchecked")
    public static <E> OldPersistentLinkedHashSet<E> of() {
        return ((OldPersistentLinkedHashSet<E>) OldPersistentLinkedHashSet.EMPTY);
    }

    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param elements elements
    /// @param <E>      the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> OldPersistentLinkedHashSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new OldPersistentLinkedHashSetBuilder<E>().addArray(elements).build();
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
    public OldPersistentLinkedHashSet<E> adding(@Nullable E element) {
        return addLast(element, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OldPersistentLinkedHashSet<E> addingAll(Iterable<? extends E> c) {
        var result = this;
        for (var e : c) {
            result = result.adding(e);
        }
        return result;
    }

    private static <E> @Nullable LinkedElement<E> get(BitmapIndexedNode<LinkedElement<E>> hashSet, @Nullable E key) {
        if (key == null) {
            return null;
        }
        Object result = hashSet.find(
                new LinkedElement<>((E) key, null, null),
                Objects.hashCode(key), 0, Objects::equals);
        return result == Node.NO_DATA ? null : (LinkedElement<E>) result;
    }

    @Override
    public OldPersistentLinkedHashSet<E> addingFirst(@Nullable E element) {
        return addFirst(element, true);
    }

    private OldPersistentLinkedHashSet<E> addFirst(@Nullable E e, boolean moveToFirst) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, null, first == null ? null : first.key());
        var newHashSet = this.root.put(null,
                linkedElement, OldPersistentLinkedHashSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToFirst && oldKey.prev() != null ? newKey : oldKey,
                Objects::equals, OldPersistentLinkedHashSet::keyHash);
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
                return new OldPersistentLinkedHashSet<>(linkedElement, updated.last, updated.hashSet, size + 1);
            }
            return new OldPersistentLinkedHashSet<>(linkedElement, updated.last, updated.hashSet, size);
        }
        return this;
    }

    @Override
    public OldPersistentLinkedHashSet<E> addingLast(@Nullable E element) {
        return addLast(element, true);
    }

    private OldPersistentLinkedHashSet<E> addLast(@Nullable E e, boolean moveToLast) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, last == null ? null : last.key(), null);
        var newHashSet = root.put(null,
                linkedElement, OldPersistentLinkedHashSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToLast && oldKey.next() != null ? newKey : oldKey,
                Objects::equals, OldPersistentLinkedHashSet::keyHash);
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
                return new OldPersistentLinkedHashSet<>(updated.first, linkedElement, updated.hashSet, size + 1);
            }
            return new OldPersistentLinkedHashSet<>(updated.first, linkedElement, updated.hashSet, size);
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
                newData, OldPersistentLinkedHashSet.keyHash(ee), 0, details,
                (oldKey, newKey) -> newKey,
                Objects::equals, OldPersistentLinkedHashSet::keyHash);
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
                newData, OldPersistentLinkedHashSet.keyHash(ee), 0, details,
                (oldKey, newKey) -> newKey,
                Objects::equals, OldPersistentLinkedHashSet::keyHash);
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
    public OldPersistentLinkedHashSet<E> cleared() {
        return of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return root.find(new LinkedElement<>((E) o, null, null), keyHash(o), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof OldPersistentLinkedHashSet<?> that) {
            return size == that.size && root.equivalent(that.root);
        }
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedElementIterator<>(first, root, LinkedElement::key);
    }

    private Iterator<E> reverseIterator() {
        return new ReversedLinkedElementIterator<>(last, root, LinkedElement::key);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public OldPersistentLinkedHashSet<E> removingFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removing(getFirst());
    }

    @Override
    public OldPersistentLinkedHashSet<E> removingLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removing(getLast());
    }

    @Override
    public OldPersistentLinkedHashSet<E> removing(E key) {
        int keyHash = keyHash(key);
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<LinkedElement<E>> newHashSet = root.remove(null,
                new LinkedElement<>(key, null, null), keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            if (size == 1) return OldPersistentLinkedHashSet.of();
            var removed = details.getOldData();
            assert removed != null;
            Updated<E> updated = new Updated<>(newHashSet, first, last);
            updated = updateNext(updated.hashSet, get(updated.hashSet, removed.prev()), removed.next(), updated.first, updated.last);
            updated = updatePrev(updated.hashSet, get(updated.hashSet, removed.next()), removed.prev(), updated.first, updated.last);
            if (updated.last == removed)
                updated = new Updated<>(updated.hashSet, updated.first, get(updated.hashSet, updated.last.prev()));
            if (updated.first == removed)
                updated = new Updated<>(updated.hashSet, get(updated.hashSet, updated.first.next()), updated.last);
            return new OldPersistentLinkedHashSet<>(updated.first, updated.last, updated.hashSet, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OldPersistentLinkedHashSet<E> removingAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public OldPersistentLinkedHashSet<E> retainingAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<E> spliterator() {
        return new ChampSpliterator<>(root, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.NONNULL);
    }

    @Override
    public OldMutableLinkedHashSet<E> toMutable() {
        return new OldMutableLinkedHashSet<>(this);
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
            return OldPersistentLinkedHashSet.copyOf(deserializedElements);
        }
    }
}