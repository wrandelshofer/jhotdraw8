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
public class VectorList<E> implements PersistentList<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final VectorList<?> EMPTY = new VectorList<>();
    final transient BitMappedTrie<E> root;

    /// Constructs a new empty list.
    protected VectorList() {
        this.root = BitMappedTrie.empty();
    }

    /// Constructs a new list that contains all the elements of
    /// the specified iterable.
    ///
    /// @param iterable an iterable
    @SuppressWarnings("unchecked")
    protected VectorList(@Nullable Iterable<? extends E> iterable) {
        if (iterable == null) {
            this.root = BitMappedTrie.empty();
        } else if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            this.root = BitMappedTrie.empty();
        } else if (iterable instanceof VectorList<? extends E> that) {
            this.root = (BitMappedTrie<E>) that.root;
        } else if (iterable instanceof MutableVectorList<? extends E> mc) {
            VectorList<? extends E> that = mc.toPersistent();
            this.root = (BitMappedTrie<E>) that.root;
        } else if (iterable instanceof Collection<?> c) {
            this.root = BitMappedTrie.ofAll(c.toArray());
        } else {
            BitMappedTrie<E> root = BitMappedTrie.<E>empty().appendAll(iterable);
            this.root = root.length() == 0 ? BitMappedTrie.empty() : root;
        }
    }


    VectorList(BitMappedTrie<E> trie) {
        this.root = trie;
    }

    /// Creates a new instance with the provided privateData data object.
    ///
    /// This constructor is intended to be called from a constructor
    /// of the subclass, that is called from method [#newInstance(PrivateData)].
    ///
    /// @param privateData an privateData data object
    protected VectorList(PrivateData privateData) {
        this.root = privateData.get();
    }

    /// Creates a new instance with the provided privateData object as its internal data structure.
    ///
    /// Subclasses must override this method, and return a new instance of their subclass!
    ///
    /// @param privateData the internal data structure needed by this class for creating the instance.
    /// @return a new instance of the subclass
    protected VectorList<E> newInstance(PrivateData privateData) {
        return new VectorList<>(privateData);
    }

    @SuppressWarnings("unchecked")
    private VectorList<E> newInstance(BitMappedTrie<E> trie) {
        return newInstance(new PrivateData(trie));
    }

    @SuppressWarnings("unchecked")
    public static <T> VectorList<T> of() {
        return (VectorList<T>) EMPTY;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> VectorList<T> of(T... t) {
        return new VectorList<>(BitMappedTrie.ofAll(t));

    }

    public static <T> VectorList<T> ofIterator(Iterator<T> iterator) {
        return VectorList.<T>of().addAll(() -> iterator);
    }

    public static <T> VectorList<T> ofStream(Stream<T> stream) {
        return VectorList.<T>of().addAll(stream::iterator);
    }

    @SuppressWarnings("unchecked")
    public static <T> VectorList<T> copyOf(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return of();
        }
        if (iterable instanceof VectorList) {
            return (VectorList<T>) iterable;
        }
        if (iterable instanceof MutableVectorList<?> mc) {
            return (VectorList<T>) mc.toPersistent();
        }
        if (iterable instanceof Collection<?> c) {
            return new VectorList<>(BitMappedTrie.ofAll(c.toArray()));
        }
        BitMappedTrie<T> root = BitMappedTrie.<T>empty().appendAll(iterable);
        return root.length() == 0 ? of() : new VectorList<>(root);
    }

    @Override
    public <T> VectorList<T> empty() {
        return of();
    }

    @Override
    public VectorList<E> add(E element) {
        return newInstance(root.append(element));
    }


    @Override
    public VectorList<E> add(int index, E element) {
        if (index == 0) {
            return newInstance(root.prepend(element));
        }
        return index == size() ? add(element) : addAll(index, Collections.singleton(element));
    }

    @Override
    public VectorList<E> addAll(Iterable<? extends E> c) {
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
    public VectorList<E> addFirst(@Nullable E element) {
        return add(0, element);
    }

    @Override
    public VectorList<E> addLast(@Nullable E element) {
        return newInstance(root.append(element));
    }

    @Override
    public VectorList<E> addAll(int index, Iterable<? extends E> c) {
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

    public VectorList<E> reverse() {
        return size() < 2 ? this : VectorList.copyOf(readableReversed());
    }

    @Override
    public VectorList<E> remove(E element) {
        int index = indexOf(element);
        return index < 0 ? this : removeAt(index);
    }

    @Override
    public VectorList<E> removeAt(int index) {
        return removeRange(index, index + 1);
    }

    @Override
    public VectorList<E> removeFirst() {
        return removeAt(0);
    }

    @Override
    public VectorList<E> removeLast() {
        return removeAt(size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorList<E> retainAll(Iterable<?> c) {
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
    public VectorList<E> removeRange(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        int size = size();
        Objects.checkIndex(toIndex, size + 1);
        if (fromIndex == 0 && toIndex == size) {
            return empty();
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
    public VectorList<E> removeAll(Iterable<?> c) {
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
    public VectorList<E> set(int index, E element) {
        BitMappedTrie<E> newRoot = root.update(index, element);
        return newRoot == this.root ? this : newInstance(newRoot);
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size());
        return root.get(index);
    }

    @Override
    public VectorList<E> readableSubList(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size() + 1);
        if (toIndex - fromIndex <= 1) {
            return empty();
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
        return new VectorList.SerializationProxy<>(this.toMutable());
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
            return VectorList.of().addAll(deserializedElements);
        }
    }


}
