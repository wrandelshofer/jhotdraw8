/*
 * @(#)ChampTrieGraphviz.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champset;

import org.jhotdraw8.annotation.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static java.lang.Math.min;

/**
 * Dumps a CHAMP trie in the Graphviz DOT language.
 * <p>
 * References:
 * <dl>
 *     <dt>Graphviz. DOT Language.</dt>
 *     <dd><a href="https://graphviz.org/doc/info/lang.html">graphviz.org</a></dd>
 * </dl>
 *
 * @param <K> the key type
 */
public class ChampTrieGraphviz<K> {

    private void dumpBitmapIndexedNodeSubTree(@NonNull Appendable a, @NonNull BitmapIndexedNode<K> node, int shift, int keyHash) throws IOException {

        // Print the node as a record with a compartment for each child element (node or data)
        String id = toNodeId(keyHash, shift);
        a.append('n');
        a.append(id);
        a.append(" [label=\"");
        boolean first = true;


        int nodeMap = node.nodeMap();
        int dataMap = node.dataMap();


        int combinedMap = nodeMap | dataMap;
        for (int i = 0, n = Integer.bitCount(combinedMap); i < n; i++) {
            int mask = combinedMap & (1 << i);
        }

        for (int mask = 0; mask <= org.jhotdraw8.collection.champset.Node.BIT_PARTITION_MASK; mask++) {
            int bitpos = org.jhotdraw8.collection.champset.Node.bitpos(mask);
            if (((nodeMap | dataMap) & bitpos) != 0) {
                if (first) {
                    first = false;
                } else {
                    a.append('|');
                }
                a.append("<f");
                a.append(Integer.toString(mask));
                a.append('>');
                if ((dataMap & bitpos) != 0) {
                    a.append(Objects.toString(node.getKey(org.jhotdraw8.collection.champset.Node.index(dataMap, bitpos))));
                } else {
                    a.append("·");
                }
            }
        }
        a.append("\"];\n");

        for (int mask = 0; mask <= org.jhotdraw8.collection.champset.Node.BIT_PARTITION_MASK; mask++) {
            int bitpos = org.jhotdraw8.collection.champset.Node.bitpos(mask);
            int subNodeKeyHash = (mask << shift) | keyHash;

            if ((nodeMap & bitpos) != 0) { // node (not value)
                // Print the sub-node
                final org.jhotdraw8.collection.champset.Node<K> subNode = node.nodeAt(bitpos);
                dumpSubTrie(a, subNode, shift + org.jhotdraw8.collection.champset.Node.BIT_PARTITION_SIZE, subNodeKeyHash);

                // Print an arrow to the sub-node
                a.append('n');
                a.append(id);
                a.append(":f");
                a.append(Integer.toString(mask));
                a.append(" -> n");
                a.append(toNodeId(subNodeKeyHash, shift + org.jhotdraw8.collection.champset.Node.BIT_PARTITION_SIZE));
                a.append(" [label=\"");
                a.append(toArrowId(mask, shift));
                a.append("\"];\n");
            }
        }
    }

    private void dumpHashCollisionNodeSubTree(@NonNull Appendable a, @NonNull HashCollisionNode<K> node, int shift, int keyHash) throws IOException {
        // Print the node as a record
        a.append("n").append(toNodeId(keyHash, shift));
        a.append(" [color=red;label=\"");
        boolean first = true;

        @NonNull Object[] nodes = node.keys;
        for (int i = 0, index = 0; i < nodes.length; i += 1, index++) {
            if (first) {
                first = false;
            } else {
                a.append('|');
            }
            a.append("<f");
            a.append(Integer.toString(index));
            a.append('>');
            a.append(Objects.toString(nodes[i]));
        }
        a.append("\"];\n");
    }

    private void dumpSubTrie(@NonNull Appendable a, Node<K> node, int shift, int keyHash) throws IOException {
        if (node instanceof org.jhotdraw8.collection.champset.BitmapIndexedNode) {
            dumpBitmapIndexedNodeSubTree(a, (BitmapIndexedNode<K>) node,
                    shift, keyHash);
        } else {
            dumpHashCollisionNodeSubTree(a, (org.jhotdraw8.collection.champset.HashCollisionNode<K>) node,
                    shift, keyHash);

        }

    }

    /**
     * Dumps a CHAMP Trie in the Graphviz DOT language.
     *
     * @param a    an {@link Appendable}
     * @param root the root node of the trie
     */
    public void dumpTrie(@NonNull Appendable a, Node<K> root) throws IOException {
        a.append("digraph ChampTrie {\n");
        a.append("node [shape=record];\n");
        dumpSubTrie(a, root, 0, 0);
        a.append("}\n");
    }

    /**
     * Dumps a CHAMP Trie in the Graphviz DOT language.
     *
     * @param root the root node of the trie
     * @return the dumped trie
     */
    public @NonNull String dumpTrie(Node<K> root) {
        StringBuilder a = new StringBuilder();
        try {
            dumpTrie(a, root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return a.toString();
    }

    private @NonNull String toArrowId(int mask, int shift) {
        String id = Integer.toBinaryString((mask) & org.jhotdraw8.collection.champset.Node.BIT_PARTITION_MASK);
        StringBuilder buf = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = id.length(); i < min(org.jhotdraw8.collection.champset.Node.HASH_CODE_LENGTH - shift, Node.BIT_PARTITION_SIZE); i++) {
            buf.append('0');
        }
        buf.append(id);
        return buf.toString();
    }

    private @NonNull String toNodeId(int keyHash, int shift) {
        if (shift == 0) {
            return "root";
        }
        String id = Integer.toBinaryString(keyHash);
        StringBuilder buf = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = id.length(); i < shift; i++) {
            buf.append('0');
        }
        buf.append(id);
        return buf.toString();
    }
}
