/*
 * @(#)SimpleImmutableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlyListFacade;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.icollection.serialization.ListSerializationProxy;

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
public class SimpleImmutableList<E> implements ImmutableList<E>, Serializable {
    private static final SimpleImmutableList<?> EMPTY = new SimpleImmutableList<>();
    final @NonNull BitMappedTrie<E> trie;

    /**
     * Constructs a new empty list.
     */
    protected SimpleImmutableList() {
        this.trie = BitMappedTrie.empty();
    }

    /**
     * Constructs a new list that contains all the elements of
     * the specified iterable.
     *
     * @param iterable an iterable
     */
    @SuppressWarnings("unchecked")
    protected SimpleImmutableList(final @Nullable Iterable<? extends E> iterable) {
        if (iterable == null) {
            this.trie = BitMappedTrie.empty();
        } else if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            this.trie = BitMappedTrie.empty();
        } else if (iterable instanceof SimpleImmutableList<? extends E> that) {
            this.trie = (BitMappedTrie<E>) that.trie;
        } else if (iterable instanceof SimpleMutableList<? extends E> mc) {
            SimpleImmutableList<? extends E> that = mc.toImmutable();
            this.trie = (BitMappedTrie<E>) that.trie;
        } else if (iterable instanceof Collection<?> c) {
            this.trie = BitMappedTrie.ofAll(c.toArray());
        } else {
            BitMappedTrie<E> root = BitMappedTrie.<E>empty().appendAll(iterable);
            this.trie = root.length() == 0 ? BitMappedTrie.empty() : root;
        }
    }


    SimpleImmutableList(BitMappedTrie<E> trie) {
        this.trie = trie;
    }

    /**
     * Creates a new instance with the provided opaque data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(Opaque)}.
     *
     * @param opaque an opaque data object
     */
    protected SimpleImmutableList(@NonNull Opaque opaque) {
        this.trie = opaque.get();
    }

    /**
     * Creates a new instance with the provided opaque object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param opaque the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected @NonNull SimpleImmutableList<E> newInstance(@NonNull Opaque opaque) {
        return new SimpleImmutableList<>(opaque);
    }

    @SuppressWarnings("unchecked")
    private @NonNull SimpleImmutableList<E> newInstance(@NonNull BitMappedTrie<E> trie) {
        return newInstance(new Opaque(trie));
    }

    @SuppressWarnings("unchecked")
    public static <T> SimpleImmutableList<T> of() {
        return (SimpleImmutableList<T>) EMPTY;
    }

    @SafeVarargs
    public static <T> SimpleImmutableList<T> of(T... t) {
        return new SimpleImmutableList<>(BitMappedTrie.ofAll(t));

    }

    @SuppressWarnings("unchecked")
    public static <T> SimpleImmutableList<T> copyOf(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return of();
        }
        if (iterable instanceof SimpleImmutableList) {
            return (SimpleImmutableList<T>) iterable;
        }
        if (iterable instanceof SimpleMutableList<?> mc) {
            return (SimpleImmutableList<T>) mc.toImmutable();
        }
        if (iterable instanceof Collection<?> c) {
            return new SimpleImmutableList<>(BitMappedTrie.ofAll(c.toArray()));
        }
        BitMappedTrie<T> root = BitMappedTrie.<T>empty().appendAll(iterable);
        return root.length() == 0 ? of() : new SimpleImmutableList<>(root);
    }

    @Override
    public @NonNull SimpleImmutableList<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull SimpleImmutableList<E> add(@NonNull E element) {
        return newInstance(trie.append(element));
    }

    @Override
    public @NonNull SimpleImmutableList<E> add(int index, @NonNull E element) {
        if (index == 0) {
            return newInstance(trie.prepend(element));
        }
        return index == size() ? add(element) : addAll(index, Collections.singleton(element));
    }

    @Override
    public @NonNull SimpleImmutableList<E> addAll(@NonNull Iterable<? extends E> c) {
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
    public @NonNull SimpleImmutableList<E> addFirst(@Nullable E element) {
        return add(0, element);
    }

    @Override
    public @NonNull SimpleImmutableList<E> addLast(@Nullable E element) {
        return newInstance(trie.append(element));
    }

    @Override
    public @NonNull SimpleImmutableList<E> addAll(int index, @NonNull Iterable<? extends E> c) {
        Objects.requireNonNull(c, "c is null");
        if (index >= 0 && index <= size()) {
            final SimpleImmutableList<E> begin = readOnlySubList(0, index).addAll(c);
            final SimpleImmutableList<E> end = readOnlySubList(index, size());
            return begin.addAll(end);
        } else {
            throw new IndexOutOfBoundsException("addAll(" + index + ", c) on Vector of size " + size());
        }
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                () -> size(),
                index -> get(size() - 1 - index),
                () -> this);
    }

    public @NonNull SimpleImmutableList<E> reverse() {
        return size() < 2 ? this : SimpleImmutableList.copyOf(readOnlyReversed());
    }

    @Override
    public @NonNull SimpleImmutableList<E> remove(@NonNull E element) {
        int index = indexOf(element);
        return index < 0 ? this : removeAt(index);
    }

    @Override
    public @NonNull SimpleImmutableList<E> removeAt(int index) {
        return removeRange(index, index + 1);
    }

    @Override
    public @NonNull SimpleImmutableList<E> removeFirst() {
        return (SimpleImmutableList<E>) ImmutableList.super.removeFirst();
    }

    @Override
    public SimpleImmutableList<E> removeLast() {
        return (SimpleImmutableList<E>) ImmutableList.super.removeLast();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SimpleImmutableList<E> retainAll(@NonNull Iterable<?> c) {
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
    public @NonNull SimpleImmutableList<E> removeRange(int fromIndex, int toIndex) {
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
    public @NonNull SimpleImmutableList<E> removeAll(@NonNull Iterable<?> c) {
        if (isEmpty()) return this;
        SimpleImmutableList<E> result = this;
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
    public @NonNull SimpleImmutableList<E> set(int index, @NonNull E element) {
        BitMappedTrie<E> newRoot = trie.update(index, element);
        return newRoot == this.trie ? this : newInstance(newRoot);
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size());
        return trie.get(index);
    }

    @Override
    public @NonNull SimpleImmutableList<E> readOnlySubList(int fromIndex, int toIndex) {
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
            if (Objects.equals(e, o)) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ReadOnlyList.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull SimpleMutableList<E> toMutable() {
        return new SimpleMutableList<>(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SimpleImmutableList.SerializationProxy<>(this.toMutable());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return trie.iterator(0, size());
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
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

        protected SerializationProxy(@NonNull List<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return SimpleImmutableList.of().addAll(deserializedElements);
        }
    }


}
