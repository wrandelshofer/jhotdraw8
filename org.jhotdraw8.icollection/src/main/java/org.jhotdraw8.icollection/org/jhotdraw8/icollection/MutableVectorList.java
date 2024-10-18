/*
 * @(#)SimpleMutableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableListFacade;
import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedListView;
import org.jhotdraw8.icollection.serialization.ListSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Implements the {@link List} interface using a bit-mapped trie (Vector).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 elements</li>
 *     <li>allows null elements</li>
 *     <li>is persistent</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order of the list</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>addLast: O(log N)</li>
 *     <li>set: O(log N)</li>
 *     <li>removeAt: O(N)</li>
 *     <li>removeFirst,removeLast: O(log N)</li>
 *     <li>contains: O(N)</li>
 *     <li>toPersistent: O(1)</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * References:
 * <p>
 * This class has been derived from Vavr Vector.java.
 * <dl>
 *     <dt>Vector.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Vector.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class MutableVectorList<E> extends AbstractList<E> implements Serializable, ReadableList<E>, List<E>, Cloneable {
    @Serial
    private static final long serialVersionUID = 0L;

    private transient BitMappedTrie<E> root;

    /**
     * Constructs a new empty list.
     */
    public MutableVectorList() {
        root = BitMappedTrie.empty();
    }

    @Override
    public void addFirst(E e) {
        root = root.prepend(Collections.singleton(e).iterator(), 1);
        modCount++;
    }

    @Override
    public void addLast(E e) {
        root = root.append(e);
        modCount++;
    }

    @Override
    public ReadableSequencedCollection<E> readOnlyReversed() {
        return new ReadableListFacade<>(
                this::size,
                index -> get(root.length - 1 - index),
                () -> this
        );
    }

    @Override
    public List<E> reversed() {
        return new ReversedListView<>(this, this::modCount);
    }

    private int modCount() {
        return modCount;
    }

    @Override
    public int size() {
        return root.length;
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, root.length);
        return root.get(index);
    }

    @Override
    public E getFirst() {
        return ReadableList.super.getFirst();
    }

    @Override
    public E getLast() {
        return ReadableList.super.getLast();
    }

    @Override
    public ReadableList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new ReadableListFacade<>(() -> toIndex - fromIndex, i -> get(i - fromIndex));
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        Objects.checkIndex(index, root.length + 1);
        int oldSize = root.length;
        VectorList<E> persistent = toPersistent().addAll(index, c);
        if (oldSize != persistent.size()) {
            root = persistent.trie;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Adds all elements in the specified collection at the specified position.
     *
     * @param index the insertion position
     * @param c     the collection to be added to ths list
     * @return {@code true} if this list changed as a result of the call
     */
    public boolean addAll(int index, Iterable<? extends E> c) {
        Objects.checkIndex(index, root.length + 1);
        int oldSize = root.length;
        VectorList<E> persistent = toPersistent().addAll(index, c);
        if (oldSize != persistent.size()) {
            root = persistent.trie;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Adds all elements in the specified collection at the end of this list.
     *
     * @param c the collection to be added to ths list
     * @return {@code true} if this list changed as a result of the call
     */
    public boolean addAll(Iterable<? extends E> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int oldSize = root.length;
        VectorList<E> persistent = toPersistent().removeAll(c);
        if (oldSize != persistent.size()) {
            root = persistent.trie;
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = root.length;
        VectorList<E> persistent = toPersistent().retainAll(c);
        if (oldSize != persistent.size()) {
            root = persistent.trie;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Constructs a list containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableVectorList(Iterable<? extends E> c) {
        if (c instanceof MutableVectorList<?>) {
            c = ((MutableVectorList<? extends E>) c).toPersistent();
        }
        if (c instanceof VectorList<?>) {
            VectorList<E> that = (VectorList<E>) c;
            this.root = that.trie;
        } else {
            this.root = BitMappedTrie.empty();
            addAll(0, c);
        }
    }

    public VectorList<E> toPersistent() {
        return root.length == 0 ? VectorList.of() : new VectorList<>(root);
    }

    @Serial
    private Object writeReplace() {
        return new MutableVectorList.SerializationProxy<>(this);
    }

    @Override
    public boolean add(E e) {
        root = root.append(e);
        modCount++;
        return true;

    }

    @Override
    public E set(int index, E element) {
        Objects.checkIndex(index, root.length);
        E oldValue = get(index);
        root = root.update(index, element);

        // According to Guava Tests, this method must not affect modCount!
        // modCount++;

        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        Objects.checkIndex(index, root.length + 1);
        if (index == root.length) {
            add(element);
        } else {
            addAll(index, Collections.singleton(element));
        }
    }

    @Override
    public E remove(int index) {
        Objects.checkIndex(index, root.length);
        E removed = get(index);
        removeRange(index, index + 1);
        return removed;
    }

    @Override
    public Spliterator<E> spliterator() {
        return root.spliterator(0, size(), Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        root = toPersistent().removeRange(fromIndex, toIndex).trie;
        modCount++;
    }

    @Override
    public MutableVectorList<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            MutableVectorList<E> clone = (MutableVectorList<E>) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
            return new MutableVectorList<>(deserializedElements);
        }
    }
}
