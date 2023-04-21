/*
 * @(#)TrieArray.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.rrb;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.rrb.TrieListHelper.InnerTrieNode;
import org.jhotdraw8.collection.rrb.TrieListHelper.LeafTrieNode;
import org.jhotdraw8.collection.rrb.TrieListHelper.TrieNode;

import static org.jhotdraw8.collection.rrb.TrieListHelper.BIT_PARTITION_SIZE;
import static org.jhotdraw8.collection.rrb.TrieListHelper.M;

/**
 * A mutable array with a fixed number of elements.
 * <p>
 * This implementation is based on a left-wise dense radix trie, using element
 * indices as keys.
 * <p>
 * Invariants:
 * <ul>
 *     <li>The data structure consists of a left-wise dense trie.</li>
 *
 *     <li>The trie consists of inner nodes and leaf nodes.</li>
 *     <li>The children of an inner node are inner nodes or leaf nodes.</li>
 *
 *     <li>The children of a leaf node are data elements.</li>
 *     <li>All leaf nodes are at the same depth in the trie.</li>
 *     <li>All nodes except the right-most have {@code m} children.</li>
 *     <li>The right-most nodes have between {@code 1} and {@code m} children.</li>
 *     <li>A Trie Array with a trie of height {@code h} has at most
 *     {@code m^h} elements.</li>
 *     <li>{@code m} must be a power of 2</li>
 *     <li>A leaf nodes have a shift of {@code 0}</li>
 *     <li>The trie has {@code ⌊size / m⌋} leaf nodes.</li>
 *     <li>The trie has height {@code h = ⌈log(size - 1)/log(m)⌉}</li>
 *     <li>The root node has {@code ⌈size / h^m⌉ children}</li>
 *     <li>The root node has a shift of {@code m*h}</li>
 * </ul>
 * References:
 * <dl>
 *     <dt>Jean Niklas L'orange. (2014). Improving RRB-Tree Performance through
 *     Transience</dt>
 *     <dd><a href="https://hypirion.com/thesis.pdf">hypirion.com</a></dd>
 * </dl>
 * @param <E> the element type
 */
public class TrieArray<E> {
    private final int size;
    private final int shift;
    private final @Nullable TrieNode<E> root;

    public TrieArray(int size) {
        this.size = size;

        int m = M;

        if (size == 0) {
            root = null;
            shift = 0;
        } else {
            int log2Size = 31 - Integer.numberOfLeadingZeros(size - 1);
            int log2M = 31 - Integer.numberOfLeadingZeros(m);
            int h = (log2Size) / log2M;
            shift = BIT_PARTITION_SIZE * h;
            root = h == 0 ? new LeafTrieNode<>(size) : new InnerTrieNode<>(size, shift);
        }
    }

    public E get(int index) {
        return root.get(index, shift);
    }

    public int size() {
        return size;
    }

    public void set(int index, E value) {
        root.set(index, shift, value);
    }
}
