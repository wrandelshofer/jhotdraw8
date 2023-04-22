/*
 * @(#)VectorList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.vector;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ReadOnlyListFacade;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.collection.serialization.ListSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.jhotdraw8.collection.vector.ArrayType.obj;

/**
 * Implements an immutable list using a {@code bit-mapped trie}.
 * <p>
 * The code has been derived from Vavr Vector.java.
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
 *     <li>add: O(1)</li>
 *     <li>set: O(1)</li>
 *     <li>remove: O(n)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * References:
 * <p>
 * This class has been derived from Vavr Vector.java.
 * <dl>
 *     <dt>Vector.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Vector.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class VectorList<E> extends BitMappedTrie<E> implements ImmutableList<E>, Serializable {

    final int size;

    /**
     * Constructs a new empty list.
     */
    private VectorList() {
        super(obj(), obj().empty(), 0, 0, 0);
        size = 0;
    }

    VectorList(BitMappedTrie<E> root, int size) {
        super(root.type, root.array, root.offset, root.length, root.depthShift);
        this.size = size;
    }


    private static final VectorList<?> EMPTY = new VectorList<>();

    @SuppressWarnings("unchecked")
    public static <T> VectorList<T> of() {
        return (VectorList<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> VectorList<T> of(T... t) {
        return ((VectorList<T>) EMPTY).addAll(Arrays.asList(t));

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
        BitMappedTrie<T> root = BitMappedTrie.<T>empty().appendAll(iterable);
        return root.length() == 0 ? of() : new VectorList<T>(root, root.length);
    }

    @Override
    public @NonNull VectorList<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull VectorList<E> add(@NonNull E element) {
        return new VectorList<>(append(element), size + 1);
    }

    @Override
    public @NonNull VectorList<E> add(int index, @NonNull E element) {
        return index == size ? add(element) : addAll(index, Collections.singleton(element));
    }

    @Override
    public @NonNull VectorList<E> addAll(@NonNull Iterable<? extends E> c) {
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
            BitMappedTrie<E> root = this;
            int newSize = size;
            for (E e : c) {
                root = root.append(e);
                newSize++;
            }
            return new VectorList<>(root, newSize);
        }
        return new VectorList<>(appendAll(c), size + cSize);
    }

    @Override
    public @NonNull VectorList<E> addFirst(@Nullable E key) {
        return new VectorList<>(prepend(key), size + 1);
    }

    @Override
    public @NonNull VectorList<E> addLast(@Nullable E key) {
        return new VectorList<>(append(key), size + 1);
    }

    @Override
    public @NonNull VectorList<E> addAll(int index, @NonNull Iterable<? extends E> c) {
        Objects.requireNonNull(c, "elements is null");
        if (index >= 0 && index <= size) {
            final VectorList<E> begin = readOnlySubList(0, index).addAll(c);
            final VectorList<E> end = readOnlySubList(index, size);
            return begin.addAll(end);
        } else {
            throw new IndexOutOfBoundsException("addAll(" + index + ", c) on Vector of size " + size);
        }
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                () -> size,
                index -> get(size - 1 - index),
                () -> this);
    }

    @Override
    public @NonNull VectorList<E> remove(@NonNull E element) {
        int index = indexOf(element);
        return index < 0 ? this : removeAt(index);
    }

    @Override
    public @NonNull VectorList<E> removeAt(int index) {
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

    @Override
    public @NonNull VectorList<E> retainAll(@NonNull ReadOnlyCollection<?> c) {
        return (VectorList<E>) ImmutableList.super.retainAll(c);
    }

    @Override
    public @NonNull VectorList<E> removeRange(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size + 1);
        if (fromIndex == 0) {
            return readOnlySubList(toIndex, size);
        }
        if (toIndex == size) {
            return readOnlySubList(0, fromIndex);
        }
        final VectorList<E> begin = readOnlySubList(0, fromIndex);
        return begin.addAll(() -> listIterator(toIndex));
    }

    @Override
    public @NonNull VectorList<E> removeAll(@NonNull Iterable<?> c) {
        if (isEmpty()) return this;
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
    public @NonNull VectorList<E> retainAll(@NonNull Collection<?> c) {
        if (isEmpty()) return this;
        if (c.isEmpty()) return of();
        VectorList<E> result = this;
        for (E e : this) {
            if (!c.contains(e)) {
                result = result.remove(e);
            }
        }
        return result;
    }

    @Override
    public @NonNull VectorList<E> set(int index, @NonNull E element) {
        BitMappedTrie<E> root = update(index, element);
        return root == this ? this : new VectorList<>(root, size);
    }

    @Override
    public E get(int index) {
        return super.get(index);
    }

    @Override
    public @NonNull VectorList<E> readOnlySubList(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size + 1);
        BitMappedTrie<E> root = this;
        if (toIndex < size) {
            root = root.take(toIndex);
        }
        if (fromIndex > 0) {
            root = root.drop(fromIndex);
        }
        return root == this ? this : new VectorList<>(root, toIndex - fromIndex);
    }

    @Override
    public int size() {
        return size;
    }

    public int indexOf(Object o, int fromIndex) {
        if (fromIndex < size) {
            for (Iterator<E> i = iterator(fromIndex); i.hasNext(); fromIndex++) {
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
            if (Objects.equals(e, o)) return true;
        }
        return false;
    }

    @Override
    public @NonNull MutableVectorList<E> toMutable() {
        return new MutableVectorList<>(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new VectorList.SerializationProxy<>(this.toMutable());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return super.iterator(0);
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
            return VectorList.of().addAll(deserialized);
        }
    }

    @Override
    public int hashCode() {
        return ReadOnlyList.iteratorToHashCode(iterator());
    }

    @Override
    public boolean equals(Object obj) {
        return ReadOnlyList.listEquals(this, obj);
    }

    @Override
    public String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }


}
