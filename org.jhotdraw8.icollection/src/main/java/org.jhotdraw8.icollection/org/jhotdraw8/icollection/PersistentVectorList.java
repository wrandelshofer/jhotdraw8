/*
 * @(#)SimplePersistentList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableListFacade;
import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.serialization.ListSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;

/// Implements the [PersistentList] interface using a bit-mapped trie
/// (Vector).
///
/// The code has been derived from Vavr Vector.java.
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///   - iterates in the order of the list
///
///
/// Performance characteristics:
///
///   - addLast: O(log₃₂ N)
///   - set: O(log₃₂ N)
///   - removeAt: O(N)
///   - removeFirst,removeLast: O(log₃₂ N)
///   - contains: O(N)
///   - toMutable: O(1)
///   - clone: O(1)
///   - iterator creation: O(log₃₂ N)
///   - iterator.next: O(1)
///   - getFirst, getLast: O(log₃₂ N)
///   - reversed: O(N)
///
///
/// References:
///
/// For a similar design, see 'Vector.java' in vavr. The internal data structure of
/// this class is licensed from vavr.
///
/// [vavr Vector.java](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Vector.java)
/// [vavr MIT-License](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE)
///
/// @param <E> the element type
public class PersistentVectorList<E> implements PersistentList<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final PersistentVectorList<?> EMPTY = new PersistentVectorList<>();
    final transient BitMappedTrie<E> root;

    /// Constructs a new empty list.
    protected PersistentVectorList() {
        this.root = BitMappedTrie.empty();
    }

    /// Constructs a new list that contains all the elements of
    /// the specified iterable.
    ///
    /// @param iterable an iterable
    @SuppressWarnings("unchecked")
    protected PersistentVectorList(@Nullable Iterable<? extends E> iterable) {
        if (iterable == null) {
            this.root = BitMappedTrie.empty();
        } else if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            this.root = BitMappedTrie.empty();
        } else if (iterable instanceof PersistentVectorList<? extends E> that) {
            this.root = (BitMappedTrie<E>) that.root;
        } else if (iterable instanceof MutableVectorList<? extends E> mc) {
            PersistentVectorList<? extends E> that = mc.toPersistent();
            this.root = (BitMappedTrie<E>) that.root;
        } else if (iterable instanceof Collection<?> c) {
            this.root = BitMappedTrie.ofAll(c.toArray());
        } else {
            BitMappedTrie<E> root = BitMappedTrie.<E>empty().appendAll(iterable);
            this.root = root.length() == 0 ? BitMappedTrie.empty() : root;
        }
    }


    PersistentVectorList(BitMappedTrie<E> trie) {
        this.root = trie;
    }

    /// Creates a new instance with the provided privateData data object.
    ///
    /// This constructor is intended to be called from a constructor
    /// of the subclass, that is called from method [#newInstance(PrivateData)].
    ///
    /// @param privateData an privateData data object
    protected PersistentVectorList(PrivateData privateData) {
        this.root = privateData.get();
    }

    /// Creates a new instance with the provided privateData object as its internal data structure.
    ///
    /// Subclasses must override this method, and return a new instance of their subclass!
    ///
    /// @param privateData the internal data structure needed by this class for creating the instance.
    /// @return a new instance of the subclass
    protected PersistentVectorList<E> newInstance(PrivateData privateData) {
        return new PersistentVectorList<>(privateData);
    }

    @SuppressWarnings("unchecked")
    private PersistentVectorList<E> newInstance(BitMappedTrie<E> trie) {
        return newInstance(new PrivateData(trie));
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentVectorList<T> of() {
        return (PersistentVectorList<T>) EMPTY;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> PersistentVectorList<T> of(T... t) {
        return new PersistentVectorList<>(BitMappedTrie.ofAll(t));

    }

    public static <T> PersistentVectorList<T> ofIterator(Iterator<T> iterator) {
        return PersistentVectorList.<T>of().addingAll(() -> iterator);
    }

    public static <T> PersistentVectorList<T> ofStream(Stream<T> stream) {
        return PersistentVectorList.<T>of().addingAll(stream::iterator);
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentVectorList<T> copyOf(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return of();
        }
        if (iterable instanceof PersistentVectorList) {
            return (PersistentVectorList<T>) iterable;
        }
        if (iterable instanceof MutableVectorList<?> mc) {
            return (PersistentVectorList<T>) mc.toPersistent();
        }
        if (iterable instanceof Collection<?> c) {
            return new PersistentVectorList<>(BitMappedTrie.ofAll(c.toArray()));
        }
        BitMappedTrie<T> root = BitMappedTrie.<T>empty().appendAll(iterable);
        return root.length() == 0 ? of() : new PersistentVectorList<>(root);
    }

    @Override
    public <T> PersistentVectorList<T> cleared() {
        return of();
    }

    @Override
    public PersistentVectorList<E> adding(E element) {
        return newInstance(root.append(element));
    }


    @Override
    public PersistentVectorList<E> addingAt(int index, E element) {
        if (index == 0) {
            return newInstance(root.prepend(element));
        }
        return index == size() ? this.adding(element) : addingAllAt(index, Collections.singleton(element));
    }

    @Override
    public PersistentVectorList<E> addingAll(Iterable<? extends E> c) {
        Objects.requireNonNull(c, "iterable is null");
        if (isEmpty()) {
            return copyOf(c);
        }
        int cSize = c instanceof Collection<?> cc ? cc.size() :
                c instanceof ReadableCollection<?> rcc ? rcc.size() : -1;
        if (cSize == 0) {
            return this;
        }
        if (cSize < 0) {
            BitMappedTrie<E> newRoot = this.root;
            int newSize = size();
            for (E e : c) {
                newRoot = newRoot.append(e);
                newSize++;
            }
            return newInstance(newRoot);
        }
        return newInstance(root.appendAll(c));
    }

    @Override
    public PersistentVectorList<E> addingFirst(@Nullable E element) {
        return addingAt(0, element);
    }

    @Override
    public PersistentVectorList<E> addingLast(@Nullable E element) {
        return newInstance(root.append(element));
    }

    @Override
    public PersistentVectorList<E> addingAllAt(int index, Iterable<? extends E> c) {
        Objects.requireNonNull(c, "c is null");
        int size = size();
        if (index >= 0 && index <= size) {
            var newTrie = root.take(index).appendAll(c).append(root.iterator(index, size), size - index);
            return newInstance(newTrie);
        } else {
            throw new IndexOutOfBoundsException("addAll(" + index + ", c) on Vector of size " + size);
        }
    }

    @Override
    public ReadableSequencedCollection<E> readableReversed() {
        return new ReadableListFacade<>(
                this::size,
                index -> get(size() - 1 - index),
                () -> this);
    }

    public PersistentVectorList<E> reversed() {
        return size() < 2 ? this : PersistentVectorList.copyOf(readableReversed());
    }

    @Override
    public PersistentVectorList<E> removing(E element) {
        int index = indexOf(element);
        return index < 0 ? this : removingAt(index);
    }

    @Override
    public PersistentVectorList<E> removingAt(int index) {
        return removingRange(index, index + 1);
    }

    @Override
    public PersistentVectorList<E> removingFirst() {
        return removingAt(0);
    }

    @Override
    public PersistentVectorList<E> removingLast() {
        return removingAt(size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorList<E> retainingAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        Collection<E> set;
        if (c instanceof Collection<?> cc) {
            set = (Collection<E>) cc;
        } else if (c instanceof ReadableCollection<?> rc) {
            set = (Collection<E>) rc.asCollection();
        } else {
            set = new HashSet<>();
            c.forEach(e -> set.add((E) e));
        }
        if (set.isEmpty()) {
            return of();
        }
        var newRoot = root.filter(set::contains);
        return newRoot == root ? this : newInstance(newRoot);
    }

    @Override
    public PersistentVectorList<E> removingRange(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        int size = size();
        Objects.checkIndex(toIndex, size + 1);
        if (fromIndex == 0 && toIndex == size) {
            return this.cleared();
        }
        if (fromIndex == 0) {
            var end = root.drop(toIndex);
            return newInstance(end);
        }
        if (toIndex == size) {
            var begin = root.take(fromIndex);
            return newInstance(begin);
        }
        var newTrie = root.take(fromIndex).append(root.iterator(toIndex, size), size - toIndex);
        return newInstance(newTrie);

        // The following code does not work as expected, because prepend inserts
        // elements in reverse sequence.
        /*
        return newInstance(begin.length > end.length
                ? begin.append(end.iterator(), end.length)
                : end.prepend(begin.iterator(), begin.length),
                size - (toIndex - fromIndex));
         */
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentVectorList<E> removingAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        Collection<E> set;
        if (c instanceof Collection<?> cc) {
            set = (Collection<E>) cc;
        } else if (c instanceof ReadableCollection<?> rc) {
            set = (Collection<E>) rc.asCollection();
        } else {
            set = new HashSet<>();
            c.forEach(e -> set.add((E) e));
        }
        if (set.isEmpty()) {
            return of();
        }
        var newRoot = root.filter(o -> !set.contains(o));
        return newRoot == root ? this : newInstance(newRoot);
    }


    @Override
    public PersistentVectorList<E> replacingAt(int index, E element) {
        BitMappedTrie<E> newRoot = root.update(index, element);
        return newRoot == this.root ? this : newInstance(newRoot);
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size());
        return root.get(index);
    }

    @Override
    public PersistentVectorList<E> readableSubList(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size() + 1);
        if (toIndex - fromIndex <= 1) {
            return this.cleared();
        }
        if (fromIndex == 0 && toIndex == size()) {
            return this;
        }
        BitMappedTrie<E> newRoot = this.root.take(toIndex).drop(fromIndex);
        return newRoot == this.root ? this : newInstance(newRoot);
    }

    @Override
    public int size() {
        return root.length;
    }

    public int indexOf(Object o, int fromIndex) {
        if (fromIndex < size()) {
            for (Iterator<E> i = root.iterator(fromIndex, size()); i.hasNext(); fromIndex++) {
                E e = i.next();
                if (Objects.equals(o, e)) {
                    return fromIndex;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        for (E e : this) {
            if (Objects.equals(e, o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ReadableList.iteratorToHashCode(iterator());
    }

    @Override
    public MutableVectorList<E> toMutable() {
        return new MutableVectorList<>(this);
    }

    @Serial
    private Object writeReplace() {
        return new PersistentVectorList.SerializationProxy<>(this.toMutable());
    }

    @Override
    public Iterator<E> iterator() {
        return root.iterator(0, size());
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Spliterator<E> spliterator() {
        return root.spliterator(0, size(), Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED);
    }

    @Override
    public boolean equals(Object obj) {
        return ReadableList.listEquals(this, obj);
    }

    /// Returns a string representation of this list.
    ///
    /// The string representation is consistent with the one produced
    /// by [AbstractList#toString()].
    ///
    /// @return a string representation
    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    private static class SerializationProxy<E> extends ListSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(List<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return PersistentVectorList.of().addingAll(deserializedElements);
        }
    }


}
