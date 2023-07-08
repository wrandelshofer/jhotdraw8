/*
 * @(#)VectorList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.immutable.ImmutableList;
import org.jhotdraw8.pcollection.impl.facade.ReadOnlyListFacade;
import org.jhotdraw8.pcollection.impl.serialization.ListSerializationProxy;
import org.jhotdraw8.pcollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.pcollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.pcollection.readonly.ReadOnlyList;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedCollection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

import static org.jhotdraw8.pcollection.impl.vector.ArrayType.obj;

/**
 * Implements the {@link ImmutableList} interface using a bit-mapped trie
 * (Vector).
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
 *     <li>addLast: O(log N)</li>
 *     <li>set: O(log N)</li>
 *     <li>removeAt: O(N)</li>
 *     <li>removeFirst,removeLast: O(log N)</li>
 *     <li>contains: O(N)</li>
 *     <li>toMutable: O(1)</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(log N)</li>
 *     <li>getFirst, getLast: O(log N)</li>
 *     <li>reversed: O(N)</li>
 * </ul>
 * <p>
 * References:
 * <p>
 * For a similar design, see 'Vector.java' in vavr. Note, that this code is not a derivative
 * of that code.
 * <dl>
 *     <dt>Vector.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
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
    @SafeVarargs
    public static <T> VectorList<T> of(T... t) {
        return new VectorList<T>(BitMappedTrie.ofAll(t), t.length);

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
            return new VectorList<T>(BitMappedTrie.ofAll(c.toArray()), c.size());
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
        if (index == 0) {
            return new VectorList<>(prepend(element), size + 1);
        }
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
    public @NonNull VectorList<E> addFirst(@Nullable E element) {
        return add(0, element);
    }

    @Override
    public @NonNull VectorList<E> addLast(@Nullable E element) {
        return new VectorList<>(append(element), size + 1);
    }

    @Override
    public @NonNull VectorList<E> addAll(int index, @NonNull Iterable<? extends E> c) {
        Objects.requireNonNull(c, "c is null");
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

    public @NonNull VectorList<E> reversed() {
        return size < 2 ? this : VectorList.copyOf(readOnlyReversed());
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

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull VectorList<E> retainAll(@NonNull Iterable<?> c) {
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
        if (set.isEmpty()) return of();
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
    public @NonNull VectorList<E> removeRange(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size + 1);
        var begin = take(fromIndex);
        var end = drop(toIndex);
        return new VectorList<>(begin.length > end.length
                ? begin.append(end.iterator(), end.length)
                : end.prepend(begin.iterator(), begin.length),
                size - (toIndex - fromIndex));
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
            for (Iterator<E> i = iterator(fromIndex, size()); i.hasNext(); fromIndex++) {
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
        return super.iterator(0, size());
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        return super.spliterator(0, size(), Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED);
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
