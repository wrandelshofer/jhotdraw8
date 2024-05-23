/*
 * @(#)SimpleImmutableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlyListFacade;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
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

/**
 * Implements the {@link ImmutableList} interface using a bit-mapped trie
 * (Vector).
 * <p>
 * The code has been derived from Vavr Vector.java.
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 elements</li>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order of the list</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>addLast: O(log₃₂ N)</li>
 *     <li>set: O(log₃₂ N)</li>
 *     <li>removeAt: O(N)</li>
 *     <li>removeFirst,removeLast: O(log₃₂ N)</li>
 *     <li>contains: O(N)</li>
 *     <li>toMutable: O(1)</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(log₃₂ N)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(log₃₂ N)</li>
 *     <li>reversed: O(N)</li>
 * </ul>
 * <p>
 * References:
 * <p>
 * For a similar design, see 'Vector.java' in vavr. The internal data structure of
 * this class is licensed from vavr.
 * <dl>
 *     <dt>Vector.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Vector.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class VectorList<E> implements ImmutableList<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final VectorList<?> EMPTY = new VectorList<>();
    final transient BitMappedTrie<E> trie;

    /**
     * Constructs a new empty list.
     */
    protected VectorList() {
        this.trie = BitMappedTrie.empty();
    }

    /**
     * Constructs a new list that contains all the elements of
     * the specified iterable.
     *
     * @param iterable an iterable
     */
    @SuppressWarnings("unchecked")
    protected VectorList(final @Nullable Iterable<? extends E> iterable) {
        if (iterable == null) {
            this.trie = BitMappedTrie.empty();
        } else if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            this.trie = BitMappedTrie.empty();
        } else if (iterable instanceof VectorList<? extends E> that) {
            this.trie = (BitMappedTrie<E>) that.trie;
        } else if (iterable instanceof MutableVectorList<? extends E> mc) {
            VectorList<? extends E> that = mc.toImmutable();
            this.trie = (BitMappedTrie<E>) that.trie;
        } else if (iterable instanceof Collection<?> c) {
            this.trie = BitMappedTrie.ofAll(c.toArray());
        } else {
            BitMappedTrie<E> root = BitMappedTrie.<E>empty().appendAll(iterable);
            this.trie = root.length() == 0 ? BitMappedTrie.empty() : root;
        }
    }


    VectorList(BitMappedTrie<E> trie) {
        this.trie = trie;
    }

    /**
     * Creates a new instance with the provided privateData data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(PrivateData)}.
     *
     * @param privateData an privateData data object
     */
    protected VectorList(PrivateData privateData) {
        this.trie = privateData.get();
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
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
                || iterable instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return of();
        }
        if (iterable instanceof VectorList) {
            return (VectorList<T>) iterable;
        }
        if (iterable instanceof MutableVectorList<?> mc) {
            return (VectorList<T>) mc.toImmutable();
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
        return newInstance(trie.append(element));
    }


    @Override
    public VectorList<E> add(int index, E element) {
        if (index == 0) {
            return newInstance(trie.prepend(element));
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
                c instanceof ReadOnlyCollection<?> rcc ? rcc.size() : -1;
        if (cSize == 0) {
            return this;
        }
        if (cSize < 0) {
            BitMappedTrie<E> newRoot = this.trie;
            int newSize = size();
            for (E e : c) {
                newRoot = newRoot.append(e);
                newSize++;
            }
            return newInstance(newRoot);
        }
        return newInstance(trie.appendAll(c));
    }

    @Override
    public VectorList<E> addFirst(@Nullable E element) {
        return add(0, element);
    }

    @Override
    public VectorList<E> addLast(@Nullable E element) {
        return newInstance(trie.append(element));
    }

    @Override
    public VectorList<E> addAll(int index, Iterable<? extends E> c) {
        Objects.requireNonNull(c, "c is null");
        if (index >= 0 && index <= size()) {
            final VectorList<E> begin = readOnlySubList(0, index).addAll(c);
            final VectorList<E> end = readOnlySubList(index, size());
            return begin.addAll(end);
        } else {
            throw new IndexOutOfBoundsException("addAll(" + index + ", c) on Vector of size " + size());
        }
    }

    @Override
    public ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                this::size,
                index -> get(size() - 1 - index),
                () -> this);
    }

    public VectorList<E> reverse() {
        return size() < 2 ? this : VectorList.copyOf(readOnlyReversed());
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
        return (VectorList<E>) ImmutableList.super.removeFirst();
    }

    @Override
    public VectorList<E> removeLast() {
        return (VectorList<E>) ImmutableList.super.removeLast();
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorList<E> retainAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        final Collection<E> set;
        if (c instanceof Collection<?> cc) {
            set = (Collection<E>) cc;
        } else if (c instanceof ReadOnlyCollection<?> rc) {
            set = (Collection<E>) rc.asCollection();
        } else {
            set = new HashSet<>();
            c.forEach(e -> set.add((E) e));
        }
        if (set.isEmpty()) {
            return of();
        }
        var t = this.toMutable();
        boolean modified = false;
        for (E key : this) {
            if (!set.contains(key)) {
                t.remove(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public VectorList<E> removeRange(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size() + 1);
        var begin = trie.take(fromIndex);
        var end = trie.drop(toIndex);
        return newInstance(begin.append(end.iterator(), end.length));

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
    public VectorList<E> removeAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        VectorList<E> result = this;
        Outer:
        for (Object e : c) {
            for (int index = result.indexOf(e); index >= 0; index = result.indexOf(e, index)) {
                result = result.removeAt(index);
                if (result.isEmpty()) {
                    break Outer;
                }
            }
        }
        return result;
    }


    @Override
    public VectorList<E> set(int index, E element) {
        BitMappedTrie<E> newRoot = trie.update(index, element);
        return newRoot == this.trie ? this : newInstance(newRoot);
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size());
        return trie.get(index);
    }

    @Override
    public VectorList<E> readOnlySubList(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size() + 1);
        BitMappedTrie<E> newRoot = this.trie;
        if (toIndex < size()) {
            newRoot = newRoot.take(toIndex);
        }
        if (fromIndex > 0) {
            newRoot = newRoot.drop(fromIndex);
        }
        return newRoot == this.trie ? this : newInstance(newRoot);
    }

    @Override
    public int size() {
        return trie.length;
    }

    public int indexOf(Object o, int fromIndex) {
        if (fromIndex < size()) {
            for (Iterator<E> i = trie.iterator(fromIndex, size()); i.hasNext(); fromIndex++) {
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
        return ReadOnlyList.iteratorToHashCode(iterator());
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
        return trie.iterator(0, size());
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Spliterator<E> spliterator() {
        return trie.spliterator(0, size(), Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED);
    }

    @Override
    public boolean equals(Object obj) {
        return ReadOnlyList.listEquals(this, obj);
    }

    /**
     * Returns a string representation of this list.
     * <p>
     * The string representation is consistent with the one produced
     * by {@link AbstractList#toString()}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return ReadOnlyCollection.iterableToString(this);
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
