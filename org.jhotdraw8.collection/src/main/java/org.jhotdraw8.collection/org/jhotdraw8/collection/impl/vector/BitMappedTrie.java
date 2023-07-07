/*
 * @(#)BitMappedTrie.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */


package org.jhotdraw8.collection.impl.vector;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.function.Function.identity;
import static org.jhotdraw8.collection.impl.vector.ArrayType.obj;

/**
 * A `bit-mapped trie` is a very wide and shallow tree (for integer indices the depth will be `≤6`).
 * <p>
 * Each node has a maximum of `32` children (configurable).
 * <p>
 * Access to a given position is done by converting the index to a base 32 number and using each digit to descend down
 * the tree.
 * <p>
 * Modifying the tree is done similarly, but along the way the path is copied, returning a new root every time.
 * <p>
 * `Append` inserts in the last leaf, or if the tree is full from the right, it adds another layer on top of it
 * (the old root will be the first of the new one).
 * <p>
 * `Prepend` is done similarly, but an offset is needed, because adding a new top node (where the current root would be
 * the last node of the new root) shifts the indices by half of the current tree's full size. The `offset` shifts them
 * back to the correct index.
 * <p>
 * `Slice` is done by trimming the path from the root and discarding any `leading`/`trailing` values in effectively
 * constant time (without memory leak, as in `Java`/`Clojure`).
 * <p>
 * References:
 * <p>
 * This class has been derived from 'vavr' BitMappedTrie.java.
 * <dl>
 *     <dt>Vector.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/BitMappedTrie.java">github.com</a></dd>
 * </dl>
 */
public class BitMappedTrie<T> implements Serializable {

    static final int BRANCHING_BASE = 5;
    static final int BRANCHING_FACTOR = 1 << BRANCHING_BASE;
    static final int BRANCHING_MASK = -1 >>> -BRANCHING_BASE;

    static int firstDigit(int num, int depthShift) {
        return num >> depthShift;
    }

    static int digit(int num, int depthShift) {
        return lastDigit(firstDigit(num, depthShift));
    }

    static int lastDigit(int num) {
        return num & BRANCHING_MASK;
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private static final @NonNull BitMappedTrie<?> EMPTY = new BitMappedTrie<>(obj(), obj().empty(), 0, 0, 0);

    @SuppressWarnings("unchecked")
    public static <T> @NonNull BitMappedTrie<T> empty() {
        return (BitMappedTrie<T>) EMPTY;
    }

    public final @NonNull ArrayType<T> type;
    public final @NonNull Object array;
    public final int offset;
    public final int length;
    public final int depthShift;

    protected BitMappedTrie(@NonNull ArrayType<T> type, @NonNull Object array, int offset, int length, int depthShift) {
        this.type = type;
        this.array = array;
        this.offset = offset;
        this.length = length;
        this.depthShift = depthShift;
    }

    private static int treeSize(int branchCount, int depthShift) {
        final int fullBranchSize = 1 << depthShift;
        return branchCount * fullBranchSize;
    }

    static <T> @NonNull BitMappedTrie<T> ofAll(@NonNull Object array) {
        final ArrayType<T> type = ArrayType.of(array);
        final int size = type.lengthOf(array);
        return (size == 0) ? empty() : ofAll(array, type, size);
    }

    private static <T> @NonNull BitMappedTrie<T> ofAll(@NonNull Object array, @NonNull ArrayType<T> type, int size) {
        int shift = 0;
        for (ArrayType<T> t = type; t.lengthOf(array) > BRANCHING_FACTOR; shift += BRANCHING_BASE) {
            array = t.grouped(array, BRANCHING_FACTOR);
            t = obj();
        }
        return new BitMappedTrie<>(type, array, 0, size, shift);
    }

    private @NonNull BitMappedTrie<T> boxed() {
        return map(identity());
    }

    @SuppressWarnings("unchecked")
    @NonNull BitMappedTrie<T> prependAll(@NonNull Iterable<? extends T> iterable) {
        if (iterable instanceof SequencedCollection<?> s) {
            return prepend((Iterator<? extends T>) s._reversed().iterator(), s.size());
        }
        if (iterable instanceof ReadOnlySequencedCollection<?> c) {
            return append(iterable.iterator(), c.size());
        }

        BitMappedTrie<T> result = this;
        for (T t : iterable) {
            result = result.prepend(Collections.singleton(t).iterator(), 1);
        }
        return result;
    }

    @NonNull
    public BitMappedTrie<T> prepend(java.util.@NonNull Iterator<? extends T> iterator, int size) {
        BitMappedTrie<T> result = this;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift, offset = result.offset;
            if (result.isFullLeft()) {
                array = obj().copyUpdate(obj().empty(), BRANCHING_FACTOR - 1, array);
                shift += BRANCHING_BASE;
                offset = treeSize(BRANCHING_FACTOR - 1, shift);
            }

            final int index = offset - 1;
            final int delta = Math.min(size, lastDigit(index) + 1);
            size -= delta;

            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, prependToLeaf(iterator));
            result = new BitMappedTrie<>(type, array, offset - delta, result.length + delta, shift);
        }
        return result;
    }

    @NonNull
    public BitMappedTrie<T> prepend(@Nullable T t) {
        BitMappedTrie<T> result = this;
        int size = 1;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift, offset = result.offset;
            if (result.isFullLeft()) {
                array = obj().copyUpdate(obj().empty(), BRANCHING_FACTOR - 1, array);
                shift += BRANCHING_BASE;
                offset = treeSize(BRANCHING_FACTOR - 1, shift);
            }

            final int index = offset - 1;
            final int delta = Math.min(1, lastDigit(index) + 1);
            size -= delta;

            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, prependToLeaf(t));
            result = new BitMappedTrie<>(type, array, offset - delta, result.length + delta, shift);
        }
        return result;
    }

    private boolean isFullLeft() {
        return offset == 0;
    }

    private @NonNull NodeModifier prependToLeaf(java.util.@NonNull Iterator<? extends T> iterator) {
        return (array, index) -> {
            final Object copy = type.copy(array, BRANCHING_FACTOR);
            while (iterator.hasNext() && index >= 0) {
                type.setAt(copy, index--, iterator.next());
            }
            return copy;
        };
    }

    private @NonNull NodeModifier prependToLeaf(@Nullable T t) {
        return (array, index) -> {
            final Object copy = type.copy(array, BRANCHING_FACTOR);
            type.setAt(copy, index, t);
            return copy;
        };
    }

    @NonNull
    public BitMappedTrie<T> appendAll(@NonNull Iterable<? extends T> iterable) {
        if (iterable instanceof Collection<?> c) {
            return append(iterable.iterator(), c.size());
        }
        if (iterable instanceof ReadOnlyCollection<?> c) {
            return append(iterable.iterator(), c.size());
        }
        BitMappedTrie<T> result = this;
        for (T t : iterable) {
            result = result.append(Collections.singleton(t).iterator(), 1);
        }
        return result;
    }

    private @NonNull BitMappedTrie<T> append(java.util.@NonNull Iterator<? extends T> iterator, int size) {
        BitMappedTrie<T> result = this;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift;
            if (result.isFullRight()) {
                array = obj().asArray(array);
                shift += BRANCHING_BASE;
            }

            final int index = offset + result.length;
            final int leafSpace = lastDigit(index);
            final int delta = Math.min(size, BRANCHING_FACTOR - leafSpace);
            size -= delta;

            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, appendToLeaf(iterator, leafSpace + delta));
            result = new BitMappedTrie<>(type, array, offset, result.length + delta, shift);
        }
        return result;
    }

    @NonNull
    public BitMappedTrie<T> append(@Nullable T element) {
        BitMappedTrie<T> result = this;
        int size = 1;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift;
            if (result.isFullRight()) {
                array = obj().asArray(array);
                shift += BRANCHING_BASE;
            }

            final int index = offset + result.length;
            final int leafSpace = lastDigit(index);
            final int delta = Math.min(size, BRANCHING_FACTOR - leafSpace);
            size -= delta;

            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, appendToLeaf(element, leafSpace + delta));
            result = new BitMappedTrie<>(type, array, offset, result.length + delta, shift);
        }
        return result;
    }

    private boolean isFullRight() {
        return (offset + length + 1) > treeSize(BRANCHING_FACTOR, depthShift);
    }

    private @NonNull NodeModifier appendToLeaf(java.util.@NonNull Iterator<? extends T> iterator, int leafSize) {
        return (array, index) -> {
            final Object copy = type.copy(array, leafSize);
            while (iterator.hasNext() && index < leafSize) {
                type.setAt(copy, index++, iterator.next());
            }
            return copy;
        };
    }

    private @NonNull NodeModifier appendToLeaf(T element, int leafSize) {
        return (array, index) -> {
            final Object copy = type.copy(array, leafSize);
            if (index < leafSize) {
                type.setAt(copy, index, element);
            }
            return copy;
        };
    }

    @NonNull
    public BitMappedTrie<T> update(int index, @Nullable T element) {
        try {
            final Object root = modify(array, depthShift, offset + index, NodeModifier.COPY_NODE, updateLeafWith(type, element));
            return new BitMappedTrie<>(type, root, offset, length, depthShift);
        } catch (ClassCastException ignored) {
            return boxed().update(index, element);
        }
    }

    private @NonNull NodeModifier updateLeafWith(ArrayType<T> type, @Nullable T element) {
        return (a, i) -> type.copyUpdate(a, i, element);
    }

    @NonNull
    public BitMappedTrie<T> drop(int n) {
        if (n <= 0) {
            return this;
        } else if (n >= length) {
            return empty();
        } else {
            final int index = offset + n;
            final Object root = arePointingToSameLeaf(0, n)
                    ? array
                    : modify(array, depthShift, index, obj()::copyDrop, NodeModifier.IDENTITY);
            return collapsed(type, root, index, length - n, depthShift);
        }
    }

    @NonNull
    public BitMappedTrie<T> take(int n) {
        if (n >= length) {
            return this;
        } else if (n <= 0) {
            return empty();
        } else {
            final int index = n - 1;
            final Object root = arePointingToSameLeaf(index, length - 1)
                    ? array
                    : modify(array, depthShift, offset + index, obj()::copyTake, NodeModifier.IDENTITY);
            return collapsed(type, root, offset, n, depthShift);
        }
    }

    private boolean arePointingToSameLeaf(int i, int j) {
        return firstDigit(offset + i, BRANCHING_BASE) == firstDigit(offset + j, BRANCHING_BASE);
    }

    /* drop root node while it has a single element */
    private static <T> @NonNull BitMappedTrie<T> collapsed(@NonNull ArrayType<T> type, Object array, int offset, int length, int shift) {
        for (; shift > 0; shift -= BRANCHING_BASE) {
            final int skippedElements = obj().lengthOf(array) - 1;
            if (skippedElements != digit(offset, shift)) {
                break;
            }
            array = obj().getAt(array, skippedElements);
            offset -= treeSize(skippedElements, shift);
        }
        return new BitMappedTrie<>(type, array, offset, length, shift);
    }

    /* descend the tree from root to leaf, applying the given modifications along the way, returning the new root */
    private @NonNull Object modify(@NonNull Object root, int depthShift, int index, @NonNull NodeModifier node, @NonNull NodeModifier leaf) {
        return (depthShift == 0)
                ? leaf.apply(root, index)
                : modifyNonLeaf(root, depthShift, index, node, leaf);
    }

    private @NonNull Object modifyNonLeaf(@NonNull Object root, int depthShift, int index, @NonNull NodeModifier node, @NonNull NodeModifier leaf) {
        int previousIndex = firstDigit(index, depthShift);
        root = node.apply(root, previousIndex);

        Object array = root;
        for (int shift = depthShift - BRANCHING_BASE; shift >= BRANCHING_BASE; shift -= BRANCHING_BASE) {
            final int prev = previousIndex;
            previousIndex = digit(index, shift);
            array = setNewNode(node, prev, array, previousIndex);
        }

        final Object newLeaf = leaf.apply(obj().getAt(array, previousIndex), lastDigit(index));
        obj().setAt(array, previousIndex, newLeaf);
        return root;
    }

    private @NonNull Object setNewNode(@NonNull NodeModifier node, int previousIndex, @NonNull Object array, int offset) {
        final Object previous = obj().getAt(array, previousIndex);
        final Object newNode = node.apply(previous, offset);
        obj().setAt(array, previousIndex, newNode);
        return newNode;
    }

    @Nullable
    public T get(int index) {
        final Object leaf = getLeaf(index);
        final int leafIndex = lastDigit(offset + index);
        return type.getAt(leaf, leafIndex);
    }

    /**
     * fetch the leaf, corresponding to the given index.
     * Node: the offset and length should be taken into consideration as there may be leading and trailing garbage.
     * Also, the returned array is mutable, but should not be mutated!
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable Object getLeaf(int index) {
        if (depthShift == 0) {
            return array;
        } else {
            return getLeafGeneral(index);
        }
    }

    private @Nullable Object getLeafGeneral(int index) {
        index += offset;
        Object leaf = obj().getAt(array, firstDigit(index, depthShift));
        for (int shift = depthShift - BRANCHING_BASE; shift > 0; shift -= BRANCHING_BASE) {
            leaf = obj().getAt(leaf, digit(index, shift));
        }
        return leaf;
    }


    public @NonNull Spliterator<T> spliterator(int fromIndex, int characteristics) {
        return new BitMappedTrieSpliterator<>(this, fromIndex, characteristics);
    }

    @NonNull
    public Iterator<T> iterator(int fromIndex) {
        return new BitMappedTrieIterator<>(this, fromIndex);
    }

    public static class BitMappedTrieSpliterator<T> extends AbstractEnumeratorSpliterator<T> {
        private final int globalLength;
        private int globalIndex;

        private int index;
        private Object leaf;
        private int length;
        private final @NonNull BitMappedTrie<T> root;

        public BitMappedTrieSpliterator(@NonNull BitMappedTrie<T> root, int fromIndex, int characteristics) {
            super(root.length - fromIndex, characteristics);
            this.root = root;
            globalLength = root.length;
            globalIndex = fromIndex;
            index = lastDigit(root.offset + globalIndex);
            leaf = root.getLeaf(globalIndex);
            length = root.type.lengthOf(leaf);
        }

        @Override
        public boolean moveNext() {
            if (globalIndex >= globalLength) {
                return false;
            }
            if (index == length) {
                setCurrentArray();
            }
            current = root.type.getAt(leaf, index);
            index++;
            globalIndex++;
            return true;
        }

        public void skip(int count) {
            globalIndex += count;
            index = lastDigit(root.offset + globalIndex);
            leaf = root.getLeaf(globalIndex);
            length = root.type.lengthOf(leaf);
        }

        private void setCurrentArray() {
            index = 0;
            leaf = root.getLeaf(globalIndex);
            length = root.type.lengthOf(leaf);
        }

    }

    private static class BitMappedTrieIterator<T> implements Iterator<T> {
        private final int globalLength;
        private int globalIndex;

        private int index;
        private Object leaf;
        private int length;
        private final @NonNull BitMappedTrie<T> root;

        public BitMappedTrieIterator(@NonNull BitMappedTrie<T> root, int fromIndex) {
            this.root = root;
            globalLength = root.length;
            globalIndex = fromIndex;
            index = lastDigit(root.offset + fromIndex);
            leaf = root.getLeaf(globalIndex);
            length = root.type.lengthOf(leaf);
        }

        @Override
        public boolean hasNext() {
            return globalIndex < globalLength;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("next() on empty iterator");
            }

            if (index == length) {
                setCurrentArray();
            }
            final T next = root.type.getAt(leaf, index);

            index++;
            globalIndex++;

            return next;
        }

        private void setCurrentArray() {
            index = 0;
            leaf = root.getLeaf(globalIndex);
            length = root.type.lengthOf(leaf);
        }
    }

    ;

    @SuppressWarnings("unchecked")
    <T2> int visit(@NonNull LeafVisitor<T2> visitor) {
        int globalIndex = 0, start = lastDigit(offset);
        for (int index = 0; index < length; ) {
            final T2 leaf = (T2) getLeaf(index);
            final int end = getMin(start, index, leaf);

            globalIndex = visitor.visit(globalIndex, leaf, start, end);

            index += end - start;
            start = 0;
        }
        return globalIndex;
    }

    private int getMin(int start, int index, @Nullable Object leaf) {
        return Math.min(type.lengthOf(leaf), start + length - index);
    }

    @NonNull BitMappedTrie<T> filter(@NonNull Predicate<? super T> predicate) {
        final Object results = type.newInstance(length());
        final int length = this.<T>visit((index, leaf, start, end) -> filter(predicate, results, index, leaf, start, end));
        return (this.length == length)
                ? this
                : BitMappedTrie.ofAll(type.copyRange(results, 0, length));
    }

    private int filter(@NonNull Predicate<? super T> predicate, @NonNull Object results, int index, T leaf, int start, int end) {
        for (int i = start; i < end; i++) {
            final T value = type.getAt(leaf, i);
            if (predicate.test(value)) {
                type.setAt(results, index++, value);
            }
        }
        return index;
    }

    <U> @NonNull BitMappedTrie<U> map(@NonNull Function<? super T, ? extends U> mapper) {
        final Object results = obj().newInstance(length);
        this.<T>visit((index, leaf, start, end) -> map(mapper, results, index, leaf, start, end));
        return BitMappedTrie.ofAll(results);
    }

    private <U> int map(@NonNull Function<? super T, ? extends U> mapper, @NonNull Object results, int index, @Nullable T leaf, int start, int end) {
        for (int i = start; i < end; i++) {
            obj().setAt(results, index++, mapper.apply(type.getAt(leaf, i)));
        }
        return index;
    }

    public int length() {
        return length;
    }
}

