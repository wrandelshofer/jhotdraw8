/*
 * @(#)MutableVectorList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.facade.ReadOnlyListFacade;
import org.jhotdraw8.pcollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.pcollection.readonly.ReadOnlyList;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.pcollection.sequenced.ReversedSequencedListView;
import org.jhotdraw8.pcollection.sequenced.SequencedList;
import org.jhotdraw8.pcollection.serialization.ListSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Implements the {@link List} interface using a bit-mapped trie (Vector).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
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
 *     <li>toImmutable: O(1)</li>
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
public class MutableVectorList<E> extends AbstractList<E> implements Serializable, ReadOnlyList<E>, SequencedList<E>, Cloneable {
    @Serial
    private static final long serialVersionUID = 0L;

    private @NonNull BitMappedTrie<E> root;
    private int size;

    /**
     * Constructs a new empty list.
     */
    public MutableVectorList() {
        root = BitMappedTrie.empty();
    }

    @Override
    public void addFirst(E e) {
        root = root.prepend(Collections.singleton(e).iterator(), 1);
        size++;
        modCount++;
    }

    @Override
    public void addLast(E e) {
        root = root.append(e);
        size++;
        modCount++;
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                this::size,
                index -> get(size - 1 - index),
                () -> this
        );
    }

    @Override
    public @NonNull SequencedList<E> _reversed() {
        return new ReversedSequencedListView<E>(this, this::modCount);
    }

    private int modCount() {
        return modCount;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size);
        return root.get(index);
    }

    @Override
    public E getFirst() {
        return ReadOnlyList.super.getFirst();
    }

    @Override
    public E getLast() {
        return ReadOnlyList.super.getLast();
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        Objects.checkIndex(index, size + 1);
        int oldSize = size;
        VectorList<E> immutable = toImmutable().addAll(index, c);
        if (oldSize != immutable.size) {
            root = immutable;
            modCount++;
            size = immutable.size;
            return true;
        }
        return false;
    }


    public boolean addAll(int index, @NonNull Iterable<? extends E> c) {
        Objects.checkIndex(index, size + 1);
        int oldSize = size;
        VectorList<E> immutable = toImmutable().addAll(index, c);
        if (oldSize != immutable.size) {
            root = immutable;
            modCount++;
            size = immutable.size;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        int oldSize = size;
        VectorList<E> immutable = toImmutable().removeAll(c);
        if (oldSize != immutable.size) {
            root = immutable;
            modCount++;
            size = immutable.size;
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        int oldSize = size;
        VectorList<E> immutable = toImmutable().retainAll(c);
        if (oldSize != immutable.size) {
            root = immutable;
            modCount++;
            size = immutable.size;
            return true;
        }
        return false;
    }

    /**
     * Constructs a list containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableVectorList(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableVectorList<?>) {
            c = ((MutableVectorList<? extends E>) c).toImmutable();
        }
        if (c instanceof VectorList<?>) {
            VectorList<E> that = (VectorList<E>) c;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = BitMappedTrie.empty();
            addAll(0, c);
        }
    }

    @NonNull
    public VectorList<E> toImmutable() {
        return size == 0 ? VectorList.of() : new VectorList<>(root, size);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new MutableVectorList.SerializationProxy<>(this);
    }

    @Override
    public boolean add(E e) {
        root = root.append(e);
        size++;
        modCount++;
        return true;

    }

    @Override
    public E set(int index, E element) {
        Objects.checkIndex(index, size);
        E oldValue = get(index);
        root = root.update(index, element);

        // According to Guava Tests, this method must not affect modCount!
        // modCount++;

        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        Objects.checkIndex(index, size + 1);
        if (index == size) {
            add(element);
        } else {
            addAll(index, Collections.singleton(element));
        }
    }

    @Override
    public E remove(int index) {
        Objects.checkIndex(index, size);
        E removed = get(index);
        removeRange(index, index + 1);
        return removed;
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        return root.spliterator(0, size(), Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        root = toImmutable().removeRange(fromIndex, toIndex);
        size -= toIndex - fromIndex;
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

        protected SerializationProxy(@NonNull List<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return new MutableVectorList<>(deserialized);
        }
    }

    @Override
    public E removeFirst() {
        return SequencedList.super.removeFirst();
    }

    @Override
    public E removeLast() {
        return SequencedList.super.removeLast();
    }
}