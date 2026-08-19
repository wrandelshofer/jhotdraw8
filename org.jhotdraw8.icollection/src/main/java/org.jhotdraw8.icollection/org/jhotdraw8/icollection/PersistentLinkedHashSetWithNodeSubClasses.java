/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.linked.LinkedElementIterator;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
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

import static org.jhotdraw8.icollection.impl.champ.Node.NO_DATA;


/// Implements the [PersistentSet] interface using a Compressed Hash-Array
/// Mapped Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N)
///   - remove: O(log₃₂ N
///   - contains: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy
///   - clone: O(1)
///   - iterator.next(): O(1)
///
///
/// Implementation details:
///
/// This set performs read and write operations of single elements in O(log₃₂ N) time,
/// and in O(log₃₂ N) space.
///
/// The CHAMP trie contains nodes that may be shared with other sets.
///
/// If a write operation is performed on a node, then this set creates a
/// copy of the node and of all parent nodes up to the root (copy-path-on-write).
///
/// This set can create a mutable copy of itself in O(1) time and O(1) space
/// using method [#toMutable()]. The mutable copy shares its nodes
/// with this set, until it has gradually replaced the nodes with exclusively
/// owned nodes.
///
/// All operations on this set can be performed concurrently, without a need for
/// synchronisation.
///
/// References:
///
/// Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>Michael J. Steindorfer (2017).
///      Efficient Persistent Collections.</dt>
///      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a></dd>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a></dd>
/// </dl>
///
/// @param <E> the element type
@SuppressWarnings("exports")
public class PersistentLinkedHashSetWithNodeSubClasses<E> implements PersistentSequencedSet<E>, Serializable {
    /// We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
    static final int SALT = 0;//new Random().nextInt();
    private static final PersistentLinkedHashSetWithNodeSubClasses<?> EMPTY = new PersistentLinkedHashSetWithNodeSubClasses<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode root;
    final int size;
    static final int DATA_LENGTH = 3;
    static final int KEY_DATA_INDEX = 0;
    static final int PREV_DATA_INDEX = 1;
    static final int NEXT_DATA_INDEX = 2;
    private final @Nullable Object @Nullable [] first;
    private final @Nullable Object @Nullable [] last;


    PersistentLinkedHashSetWithNodeSubClasses(BitmapIndexedNode root, int size) {
        this(null, null, root, size);
    }

    public PersistentLinkedHashSetWithNodeSubClasses(@Nullable Object[] first, Object[] last, BitmapIndexedNode hashSet, int size) {
        this.first = first;
        this.last = last;
        this.root = hashSet;
        this.size = size;
    }


    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param c   an iterable
    /// @param <E> the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings("unchecked")
    public static <E> PersistentLinkedHashSetWithNodeSubClasses<E> copyOf(Iterable<? extends E> c) {
        return PersistentLinkedHashSetWithNodeSubClasses.<E>of().addingAll(c);
    }

    /// Returns an empty persistent set.
    ///
    /// @param <E> the element type
    /// @return an empty persistent set
    @SuppressWarnings("unchecked")
    public static <E> PersistentLinkedHashSetWithNodeSubClasses<E> of() {
        return ((PersistentLinkedHashSetWithNodeSubClasses<E>) PersistentLinkedHashSetWithNodeSubClasses.EMPTY);
    }

    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param elements elements
    /// @param <E>      the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> PersistentLinkedHashSetWithNodeSubClasses<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new PersistentLinkedHashSetBuilderWithNodeSubClasses<E>().addArray(elements).build();
    }

    /// Update function for a set: we always keep the old entry.
    ///
    /// @param oldElement the old entry
    /// @param newElement the new entry
    /// @return always returns the old entry
    static Object[] keepOldEntry(Object[] oldElement, Object[] newElement) {
        return oldElement;
    }

    static int keyHash(Object e) {
        return SALT ^ Objects.hashCode(e);
    }

    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> adding(@Nullable E element) {
        return addLast(element, false, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentLinkedHashSetWithNodeSubClasses<E> addingAll(Iterable<? extends E> c) {
        if (isEmpty() && c instanceof PersistentLinkedHashSetWithNodeSubClasses<? extends E> s) {
            return (PersistentLinkedHashSetWithNodeSubClasses<E>) s;
        }
        var m = toMutable();
        return m.addAll(c) ? m.toPersistent() : this;
    }

    @Override
    public PersistentSequencedSet<E> addingFirst(@Nullable E element) {
        return addFirst(element, true, null);
    }

    @Override
    public PersistentSequencedSet<E> addingLast(@Nullable E element) {
        return addLast(element, true, null);
    }

    record Updated(BitmapIndexedNode root, @Nullable Object[] first,
                   @Nullable Object[] last) {
    }

    private static @Nullable Object[] get(BitmapIndexedNode hashSet, @Nullable Object key) {
        if (key == null) {
            return null;
        }
        Object result = hashSet.findData(
                key,
                Objects.hashCode(key), 0, DATA_LENGTH);
        return result == NO_DATA ? null : (Object[]) result;
    }

    PersistentLinkedHashSetWithNodeSubClasses<E> addLast(@Nullable E e, boolean moveToLast, IdentityObject mutator) {
        ChangeEvent details = new ChangeEvent();
        var newData = new Object[DATA_LENGTH];
        newData[KEY_DATA_INDEX] = e;
        newData[NEXT_DATA_INDEX] = NO_DATA;
        newData[PREV_DATA_INDEX] = last != null ? last[KEY_DATA_INDEX] : NO_DATA;
        var newHashSet = root.put(null,
                e, newData, PersistentLinkedHashSetWithNodeSubClasses.keyHash(e), 0, details,
                (Object[] oldKey, Object[] newKey) -> moveToLast && oldKey[NEXT_DATA_INDEX] != NO_DATA ? newKey : oldKey,
                PersistentLinkedHashSetWithNodeSubClasses::keyHash, DATA_LENGTH);
        if (details.isModified()) {
            PersistentLinkedHashSetWithNodeSubClasses.Updated updated = new Updated(newHashSet, first, last);
            if (details.isReplaced()) {
                var removed = details.getOldData();
                assert removed != null;
                @Nullable Object[] nextData = get(updated.root, removed[NEXT_DATA_INDEX]);
                updated = updateNext(updated.root, get(updated.root, removed[PREV_DATA_INDEX]), removed[NEXT_DATA_INDEX], updated.first, updated.last, mutator);
                updated = updatePrev(updated.root, nextData, removed[PREV_DATA_INDEX], updated.first, updated.last, mutator);
                updated = updateNext(updated.root, updated.last, e, updated.first, updated.last, mutator);
                if (removed[PREV_DATA_INDEX] == NO_DATA) {
                    // first element was moved to last, therefore our next element is now first
                    nextData[PREV_DATA_INDEX] = NO_DATA;
                    updated = new Updated(updated.root, nextData, updated.last);
                } else {
                    updated = new Updated(updated.root, updated.first, newData);
                }
            } else {
                if (size == 0) {
                    updated = new Updated(updated.root, newData, newData);
                } else if (size == 1) {
                    Object[] newFirst = updated.first.clone();
                    newFirst[NEXT_DATA_INDEX] = e;
                    updated = new Updated(updated.root, newFirst, newData);
                    updated = updateNext(updated.root, updated.first, e, updated.first, updated.last, mutator);
                } else {
                    updated = updateNext(updated.root, updated.last, e, updated.first, newData, mutator);
                }
                return new PersistentLinkedHashSetWithNodeSubClasses<>(updated.first, updated.last, updated.root, size + 1);
            }
            return new PersistentLinkedHashSetWithNodeSubClasses<>(updated.first, updated.last, updated.root, size);
        }
        return this;
    }

    PersistentLinkedHashSetWithNodeSubClasses<E> addFirst(@Nullable E e, boolean moveToFirst, IdentityObject mutator) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent details = new ChangeEvent();
        var newData = new Object[DATA_LENGTH];
        newData[KEY_DATA_INDEX] = e;
        newData[PREV_DATA_INDEX] = NO_DATA;
        newData[NEXT_DATA_INDEX] = first != null ? first[KEY_DATA_INDEX] : NO_DATA;

        var newHashSet = this.root.put(mutator, e,
                newData, PersistentLinkedHashSetWithNodeSubClasses.keyHash(e), 0, details,
                (Object[] oldKey, Object[] newKey) -> moveToFirst && oldKey[PREV_DATA_INDEX] != NO_DATA ? newKey : oldKey,
                PersistentLinkedHashSetWithNodeSubClasses::keyHash, DATA_LENGTH);
        if (details.isModified()) {
            PersistentLinkedHashSetWithNodeSubClasses.Updated updated = new Updated(newHashSet, first, last);
            if (details.isReplaced()) {
                var removed = details.getOldData();
                assert removed != null;
                @Nullable Object[] previousData = get(updated.root, removed[PREV_DATA_INDEX]);
                updated = updateNext(updated.root, previousData, removed[NEXT_DATA_INDEX], updated.first, updated.last, mutator);
                updated = updatePrev(updated.root, get(updated.root, removed[NEXT_DATA_INDEX]), removed[PREV_DATA_INDEX], updated.first, updated.last, mutator);
                updated = updatePrev(updated.root, updated.first, e, newData, updated.last, mutator);
                if (removed[NEXT_DATA_INDEX] == NO_DATA) {
                    // last element was moved to first, therefore our previous element is now the last
                    previousData[NEXT_DATA_INDEX] = NO_DATA;
                    updated = new Updated(updated.root, updated.first, previousData);
                } else {
                    updated = new Updated(updated.root, newData, updated.last);
                }
            } else {
                if (size == 0) {
                    updated = new Updated(updated.root, newData, newData);
                } else if (size == 1) {
                    Object[] newLast = updated.first.clone();
                    newLast[PREV_DATA_INDEX] = e;
                    updated = new Updated(updated.root, newData, newLast);
                    updated = updateNext(updated.root, updated.last, e, updated.first, updated.last, mutator);
                } else {
                    updated = updatePrev(updated.root, updated.first, e, newData, updated.last, mutator);
                }
                return new PersistentLinkedHashSetWithNodeSubClasses<>(updated.first, updated.last, updated.root, size + 1);
            }
            return new PersistentLinkedHashSetWithNodeSubClasses<>(updated.first, updated.last, updated.root, size);
        }
        return this;
    }

    private Updated updateNext(BitmapIndexedNode hashSet, @Nullable Object[] elem, @Nullable Object e, @Nullable Object[] first, @Nullable Object[] last, IdentityObject mutator) {
        if (elem == null) {
            return new Updated(hashSet, first, last);
        }

        Object[] newData = new Object[]{elem[KEY_DATA_INDEX], elem[PREV_DATA_INDEX], e};
        var ee = newData[KEY_DATA_INDEX];
        ChangeEvent details = new ChangeEvent();
        hashSet = hashSet.put(mutator, elem[KEY_DATA_INDEX],
                newData, keyHash(ee), 0, details,
                (oldKey, newKey) -> {
                    System.arraycopy(oldKey, 0, newKey, 0, oldKey.length);
                    newKey[NEXT_DATA_INDEX] = e;
                    return newKey;
                },

                PersistentLinkedHashSetWithNodeSubClasses::keyHash, DATA_LENGTH);
        var oldData = details.getOldData();

        if (last == oldData) {
            last = newData;
        }
        if (first == oldData) {
            first = newData;
        }
        return new Updated(hashSet, first, last);
    }

    private Updated updatePrev(BitmapIndexedNode hashSet, @Nullable Object[] elem, @Nullable Object e, @Nullable Object[] first, @Nullable Object[] last, IdentityObject mutator) {
        if (elem == null) {
            return new Updated(hashSet, first, last);
        }

        Object[] newData = new Object[]{elem[KEY_DATA_INDEX], e, elem[NEXT_DATA_INDEX]};
        var ee = newData[KEY_DATA_INDEX];
        ChangeEvent details = new ChangeEvent();
        hashSet = hashSet.put(null, elem[KEY_DATA_INDEX],
                newData, keyHash(ee), 0, details,
                (oldKey, newKey) -> {
                    System.arraycopy(oldKey, 0, newKey, 0, oldKey.length);
                    newKey[PREV_DATA_INDEX] = e;
                    return newKey;
                },
                PersistentLinkedHashSetWithNodeSubClasses::keyHash, DATA_LENGTH);
        var oldData = details.getOldData();
        if (last == oldData) {
            last = newData;
        }
        if (first == oldData) {
            first = newData;
        }
        return new Updated(hashSet, first, last);
    }

    /// {@inheritDoc}
    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> cleared() {
        return of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return root.contains((E) o, keyHash(o), 0, DATA_LENGTH);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof PersistentLinkedHashSetWithNodeSubClasses<?> that) {
            return size == that.size && root.equivalent(that.root, DATA_LENGTH);
        }
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedElementIterator<>(first, root, o -> (E) o[KEY_DATA_INDEX], DATA_LENGTH, NEXT_DATA_INDEX);
    }

    Iterator<E> reverseIterator() {
        return new LinkedElementIterator<>(last, root, o -> (E) o[KEY_DATA_INDEX], DATA_LENGTH, PREV_DATA_INDEX);
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) first[KEY_DATA_INDEX];
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) last[KEY_DATA_INDEX];
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

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


    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> removing(E key) {
        return remove(key, null);
    }

    PersistentLinkedHashSetWithNodeSubClasses<E> remove(E key, IdentityObject mutator) {
        int keyHash = keyHash(key);
        ChangeEvent details = new ChangeEvent();
        BitmapIndexedNode newHashSet = root.remove(mutator,
                key, keyHash, 0, details, DATA_LENGTH);
        if (details.isModified()) {
            if (size == 1) return PersistentLinkedHashSetWithNodeSubClasses.of();
            var removed = details.getOldData();
            assert removed != null;
            Updated updated = new Updated(newHashSet, first, last);
            @Nullable Object[] nextData = get(updated.root, removed[NEXT_DATA_INDEX]);
            @Nullable Object[] previousData = get(updated.root, removed[PREV_DATA_INDEX]);
            if (nextData == null) {
                Object[] newLast = previousData.clone();
                newLast[NEXT_DATA_INDEX] = NO_DATA;
                updated = new Updated(updated.root, updated.first, newLast);
            } else {
                updated = updatePrev(updated.root, nextData, removed[PREV_DATA_INDEX], updated.first, updated.last, mutator);
            }
            if (previousData == null) {
                Object[] newFirst = nextData.clone();
                newFirst[PREV_DATA_INDEX] = NO_DATA;
                updated = new Updated(updated.root, newFirst, updated.last);
            } else {
                updated = updateNext(updated.root, previousData, removed[NEXT_DATA_INDEX], updated.first, updated.last, mutator);
            }
            return new PersistentLinkedHashSetWithNodeSubClasses<>(updated.first, updated.last, updated.root, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> removingAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> retainingAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<E> spliterator() {
        return new ChampSpliterator<>(root, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT,
                DATA_LENGTH, KEY_DATA_INDEX);
    }

    @Override
    public MutableLinkedHashSet<E> toMutable() {
        return new MutableLinkedHashSet<>(this);
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
            return PersistentLinkedHashSetWithNodeSubClasses.copyOf(deserializedElements);
        }
    }
}