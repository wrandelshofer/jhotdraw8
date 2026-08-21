/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.champlinked.LinkedElementIterator;
import org.jhotdraw8.icollection.impl.champlinked.TrieBuilder;
import org.jhotdraw8.icollection.impl.champlinked.TrieNode;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import static org.jhotdraw8.icollection.impl.champlinked.TrieNode.NO_DATA;


/// Implements the [PersistentSequencedSet] interface using a Compressed Hash-Array
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
///   - iterator.next(): O(log₃₂ N)
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
public class PersistentLinkedHashSet<E> implements PersistentSequencedSet<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final PersistentLinkedHashSet<?> EMPTY = new PersistentLinkedHashSet<Object>(TrieNode.EMPTY, 0, null, null);
    static final int ENTRY_SIZE = 3;
    static final int KEY_INDEX = 0;
    static final int PREV_INDEX = 1;
    static final int NEXT_INDEX = 2;
    final TrieNode<E> node;
    final int size;
    final @Nullable Object @Nullable [] first;
    final @Nullable Object @Nullable [] last;

    @SuppressWarnings("unchecked")
    public static <T> PersistentLinkedHashSet<T> of() {
        return (PersistentLinkedHashSet<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentLinkedHashSet<T> of(T... elements) {
        return new PersistentLinkedHashSetBuilder<T>().addArray(elements).build();
    }

    public static <T> PersistentLinkedHashSet<T> copyOf(Iterable<T> elements) {
        return new PersistentLinkedHashSetBuilder<T>().addAll(elements).build();
    }

    public static <T> PersistentLinkedHashSetBuilder<T> builder() {
        return new PersistentLinkedHashSetBuilder<T>();
    }

    PersistentLinkedHashSet(TrieNode<E> node, int size, @Nullable Object[] first, @Nullable Object[] last) {
        this.node = node;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    @Override
    public PersistentLinkedHashSet<E> cleared() {
        return of();
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    static @Nullable Object[] updReplaceWithNewEntry(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        return newEntry;
    }

    static @Nullable Object[] updCopyPrev(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        newEntry[PREV_INDEX] = oldEntry[PREV_INDEX];
        return newEntry;
    }

    static @Nullable Object[] updCopyNext(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        newEntry[NEXT_INDEX] = oldEntry[NEXT_INDEX];
        return newEntry;
    }

    static @Nullable Object[] updKeepOldEntry(@Nullable Object[] oldEntry, @Nullable Object[] newEntry) {
        return oldEntry;
    }

    @Override
    public PersistentLinkedHashSet<E> adding(E e) {
        return adding(e, new TrieBuilder<>());
    }

    @SuppressWarnings("unchecked")
    PersistentLinkedHashSet<E> adding(E e, TrieBuilder<E, Object> builder) {
        var newNode = node;
        @Nullable Object[] newEntry = new Object[ENTRY_SIZE];
        newEntry[KEY_INDEX] = e;
        newEntry[PREV_INDEX] = last == null ? NO_DATA : last[KEY_INDEX];
        newEntry[NEXT_INDEX] = NO_DATA;
        newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updKeepOldEntry, ENTRY_SIZE);
        var newSize = size + builder.size;
        if (size == newSize) {
            return this;
        }
        if (newSize == 1) {
            return new PersistentLinkedHashSet<>(newNode, 1, newEntry, newEntry);
        }
        assert last != null & first != null;
        var updatedLast = last.clone();
        updatedLast[NEXT_INDEX] = e;
        newNode = newNode.mutablePut(updatedLast, builder, PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
        if (newSize == 2) {
            return new PersistentLinkedHashSet<>(newNode, 1, updatedLast, newEntry);
        }
        return new PersistentLinkedHashSet<>(newNode, newSize, first, newEntry);
    }

    @Override
    public PersistentLinkedHashSet<E> addingAll(Iterable<? extends E> c) {
        return addingAll(c, new TrieBuilder<>());
    }

    PersistentLinkedHashSet<E> addingAll(Iterable<? extends E> c, TrieBuilder<E, Object> builder) {
        var result = new PersistentLinkedHashSetBuilder<E>(builder).addAll(this).addAll(c).build();
        return (result.size() == size) ? this : result;
    }

    @Override
    public PersistentSequencedSet<E> addingFirst(@Nullable E e) {
        return addingFirst(e, new TrieBuilder<>());
    }

    PersistentSequencedSet<E> addingFirst(@Nullable E e, TrieBuilder<E, Object> builder) {
        if (isEmpty()) {
            return adding(e, builder);
        }
        assert last != null & first != null;
        if (Objects.equals(first[KEY_INDEX], e)) {
            return this;
        }
        var newNode = node;
        @Nullable Object[] newEntry = new Object[ENTRY_SIZE];
        newEntry[KEY_INDEX] = e;
        newEntry[NEXT_INDEX] = first[KEY_INDEX];
        newEntry[PREV_INDEX] = NO_DATA;
        newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
        var newSize = size + builder.size;
        if (size != newSize) {
            var previouslyFirst = first.clone();
            previouslyFirst[PREV_INDEX] = e;
            newNode = newNode.mutablePut(previouslyFirst, builder, PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
            if (newSize == 2) {
                return new PersistentLinkedHashSet<>(newNode, 2, newEntry, previouslyFirst);
            }
            return new PersistentLinkedHashSet<>(newNode, newSize, newEntry, last);
        } else {
            // element was moved to first
            var newFirst = newEntry.clone();
            var newLast = last;
            var oldEntry = builder.entry;
            assert oldEntry != null;
            if (oldEntry[PREV_INDEX] != NO_DATA) {
                // assign prevEntry.next to oldEntry.next
                newEntry[KEY_INDEX] = oldEntry[PREV_INDEX];
                newEntry[NEXT_INDEX] = oldEntry[NEXT_INDEX];
                newEntry[PREV_INDEX] = null;
                newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyPrev, ENTRY_SIZE);
                if (oldEntry[NEXT_INDEX] == NO_DATA) {
                    // the nextEntry becomes the last element of the list
                    newLast = newEntry.clone();
                }
            }
            if (oldEntry[NEXT_INDEX] != NO_DATA) {
                // assign nextEntry.prev to oldEntry.prev
                newEntry[KEY_INDEX] = oldEntry[NEXT_INDEX];
                newEntry[NEXT_INDEX] = null;
                newEntry[PREV_INDEX] = oldEntry[PREV_INDEX];
                newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyNext, ENTRY_SIZE);
                if (newEntry[NEXT_INDEX] == NO_DATA) {
                    // the prevEntry becomes the last element of the list
                    newLast = newEntry.clone();
                }
            }
            newEntry[KEY_INDEX] = first[KEY_INDEX];
            newEntry[PREV_INDEX] = e;
            newEntry[NEXT_INDEX] = null;
            newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyNext, ENTRY_SIZE);

            return new PersistentLinkedHashSet<>(newNode, newSize, newFirst, newLast);
        }
    }

    @Override
    public PersistentSequencedSet<E> addingLast(@Nullable E e) {
        return addingLast(e, new TrieBuilder<>());
    }

    PersistentSequencedSet<E> addingLast(@Nullable E e, TrieBuilder<E, Object> builder) {
        if (isEmpty()) {
            return adding(e, builder);
        }
        assert last != null & first != null;
        if (Objects.equals(last[KEY_INDEX], e)) {
            return this;
        }
        var newNode = node;
        @Nullable Object[] newEntry = new Object[ENTRY_SIZE];
        newEntry[KEY_INDEX] = e;
        newEntry[PREV_INDEX] = last[KEY_INDEX];
        newEntry[NEXT_INDEX] = NO_DATA;
        newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
        var newSize = size + builder.size;
        if (size != newSize) {
            var previouslyLast = last.clone();
            previouslyLast[NEXT_INDEX] = e;
            newNode = newNode.mutablePut(previouslyLast, builder, PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
            if (newSize == 2) {
                return new PersistentLinkedHashSet<>(newNode, 2, previouslyLast, newEntry);
            }
            return new PersistentLinkedHashSet<>(newNode, newSize, first, newEntry);
        } else {
            // element was moved to last
            var newLast = newEntry.clone();
            var newFirst = first;
            var oldEntry = builder.entry;
            assert oldEntry != null;
            if (oldEntry[NEXT_INDEX] != NO_DATA) {
                // assign nextEntry.prev to oldEntry.prev
                newEntry[KEY_INDEX] = oldEntry[NEXT_INDEX];
                newEntry[PREV_INDEX] = oldEntry[PREV_INDEX];
                newEntry[NEXT_INDEX] = null;
                newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyNext, ENTRY_SIZE);
                if (oldEntry[PREV_INDEX] == NO_DATA) {
                    // the prevEntry becomes the first element of the list
                    newFirst = newEntry.clone();
                }
            }
            if (oldEntry[PREV_INDEX] != NO_DATA) {
                // assign prevEntry.next to oldEntry.next
                newEntry[KEY_INDEX] = oldEntry[PREV_INDEX];
                newEntry[PREV_INDEX] = null;
                newEntry[NEXT_INDEX] = oldEntry[NEXT_INDEX];
                newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyPrev, ENTRY_SIZE);
                if (newEntry[PREV_INDEX] == NO_DATA) {
                    // the prevEntry becomes the first element of the list
                    newFirst = newEntry.clone();
                }
            }
            newEntry[KEY_INDEX] = last[KEY_INDEX];
            newEntry[NEXT_INDEX] = e;
            newEntry[PREV_INDEX] = null;
            newNode = newNode.mutablePut(newEntry, builder.reset(), PersistentLinkedHashSet::updCopyPrev, ENTRY_SIZE);

            return new PersistentLinkedHashSet<>(newNode, newSize, newFirst, newLast);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashSet<E> removingAll(Iterable<?> c) {
        return removingAll(c, new TrieBuilder<>());
    }

    PersistentLinkedHashSet<E> removingAll(Iterable<?> c, TrieBuilder<E, Object> builder) {
        var newSelf = this;
        for (Object e : c) {
            newSelf = newSelf.removing((E) e, builder);
        }
        return newSelf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentLinkedHashSet<E> retainingAll(Iterable<?> c) {
        return retainingAll(c, new TrieBuilder<>());
    }

    public PersistentLinkedHashSet<E> retainingAll(Iterable<?> c, TrieBuilder<E, Object> builder) {
        if (c instanceof ReadableCollection<?> rc) {
            c = rc.asCollection();
        }
        if (!(c instanceof Collection<?>)) {
            var tmp = new HashSet<>();
            for (Object o : c) {
                tmp.add(o);
            }
            c = tmp;
        }
        var cc = (Collection<?>) c;
        var newSelf = this;
        for (E e : this) {
            if (!cc.contains(e)) {
                newSelf = newSelf.removing((E) e, builder);
            }
        }
        return newSelf;
    }

    @Override
    public PersistentLinkedHashSet<E> removing(E e) {
        return removing(e, new TrieBuilder<>());
    }


    PersistentLinkedHashSet<E> removing(E e, TrieBuilder<E, Object> builder) {
        if (isEmpty()) {
            return this;
        }
        var newNode = node.mutableRemove(Objects.hashCode(e), (E) e, 0, builder.reset(), ENTRY_SIZE);
        var newSize = size + builder.size;
        if (newSize == size) {
            return this;
        }
        if (newSize == 0) {
            return of();
        }
        assert last != null & first != null;
        var removedEntry = builder.entry;
        @Nullable Object[] newEntry = new Object[ENTRY_SIZE];
        @Nullable Object[] newFirst = first;
        @Nullable Object[] newLast = last;
        Object prevKey = removedEntry[PREV_INDEX];
        Object nextKey = removedEntry[NEXT_INDEX];
        if (prevKey != NO_DATA) {
            newEntry[KEY_INDEX] = prevKey;
            newEntry[PREV_INDEX] = null;
            newEntry[NEXT_INDEX] = nextKey;
            newNode = newNode.mutablePut(newEntry,
                    builder, PersistentLinkedHashSet::updCopyPrev, ENTRY_SIZE);
            if (newEntry[PREV_INDEX] == NO_DATA) {
                newFirst = newEntry.clone();
            }
            if (nextKey == NO_DATA) {
                newLast = newEntry.clone();
            }
        }
        if (nextKey != NO_DATA) {
            newEntry[KEY_INDEX] = nextKey;
            newEntry[PREV_INDEX] = prevKey;
            newEntry[NEXT_INDEX] = null;
            newNode = newNode.mutablePut(newEntry,
                    builder, PersistentLinkedHashSet::updCopyNext, ENTRY_SIZE);
            if (newEntry[NEXT_INDEX] == NO_DATA) {
                newLast = newEntry.clone();
            }
            if (prevKey == NO_DATA) {
                newFirst = newEntry.clone();
            }
        }
        return new PersistentLinkedHashSet<>(newNode, newSize, newFirst, newLast);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        return node.containsKey(Objects.hashCode(o), (E) o, 0, ENTRY_SIZE);
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedElementIterator<>(node, first, o -> (E) o[0], ENTRY_SIZE, NEXT_INDEX);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }


    Iterator<E> reverseIterator() {
        return new LinkedElementIterator<>(node, last, o -> (E) o[0], ENTRY_SIZE, PREV_INDEX);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        assert first != null;
        return (E) first[KEY_INDEX];
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        assert last != null;
        return (E) last[KEY_INDEX];
    }

    Spliterator<E> reverseSpliterator() {
        return Spliterators.spliterator(reverseIterator(), size, Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.IMMUTABLE);
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
            return PersistentLinkedHashSet.builder().addAll(deserializedElements).build();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return ReadableSet.setEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(this.iterator());
    }

    public MutableLinkedHashSet<E> toMutable() {
        return new MutableLinkedHashSet<>(this);
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

    public MutableLinkedHashSet<E> asSet() {
        return new MutableLinkedHashSet<>(this);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }
}