package org.jhotdraw8.collection;

import org.jhotdraw8.collection.PersistentTrieListHelper.InnerTrieNode;
import org.jhotdraw8.collection.PersistentTrieListHelper.LeafTrieNode;
import org.jhotdraw8.collection.PersistentTrieListHelper.TrieNode;

import static org.jhotdraw8.collection.PersistentTrieListHelper.BIT_PARTITION_SIZE;
import static org.jhotdraw8.collection.PersistentTrieListHelper.M;

/**
 * A mutable array with a fixed number of elements.
 * <p>
 * This implementation is based on a left-wise dense radix trie, using element
 * indices as keys.
 * <p>
 * Invariants:
 * <ul>
 *     <li>The data structure consists of a trie.</li>
 *
 *     <li>The trie consists of inner nodes and leaf nodes.</li>
 *     <li>The children of an inner node are inner nodes or leaf nodes.</li>
 *
 *     <li>The children of a leaf node are data elements.</li>
 *     <li>All leaf nodes are at the same depth in the trie.</li>
 *     <li>All nodes except the root have {@code m} children.</li>
 *     <li>The root node has between {@code 1} and {@code m} children.</li>
 *     <li>A Trie Array with a trie of height {@code h} has
 *     {@code m^h + |tail|} elements.</li>
 *     <li>{@code m} must be a power of 2</li>
 *     <li>A leaf has a shift of {@code 0}</li>
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
 */
public class TrieArray<E> {
    private final int size;
    private final int shift;
    private final TrieNode<E> root;


    @SuppressWarnings({"unchecked"})
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
            System.out.println("h.float:" + Math.log(size - 1) / Math.log(m));
            System.out.println("log2Size:" + log2Size + " log2M:" + log2M);
            System.out.println("size:" + size + " h:" + h + " nodeCapacity:" + (m << shift));

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
