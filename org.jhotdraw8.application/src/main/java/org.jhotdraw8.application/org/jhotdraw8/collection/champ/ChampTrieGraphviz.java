/*
 * @(#)ChampTrieGraphviz.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

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
 * @param <V> the value type
 */
public class ChampTrieGraphviz<K, V> {

    private void dumpBitmapIndexedNodeSubTree(@NonNull Appendable a, @NonNull BitmapIndexedNode<K, V> node, int entryLength, boolean printValue, boolean printSequenceNumber, int shift, int keyHash) throws IOException {

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

        for (int mask = 0; mask <= Node.BIT_PARTITION_MASK; mask++) {
            int bitpos = Node.bitpos(mask);
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
                    a.append(Objects.toString(node.getKey(Node.index(dataMap, bitpos), entryLength)));
                    if (printValue) {
                        a.append('=');
                        a.append(Objects.toString(node.getValue(Node.index(dataMap, bitpos), entryLength, 2)));
                    }
                    if (printSequenceNumber) {
                        a.append(" #");
                        a.append(Long.toString(node.getSequenceNumber(Node.index(dataMap, bitpos), entryLength, 1 + (printValue ? 1 : 0))));
                    }
                } else {
                    a.append("·");
                }
            }
        }
        a.append("\"];\n");

        for (int mask = 0; mask <= Node.BIT_PARTITION_MASK; mask++) {
            int bitpos = Node.bitpos(mask);
            int subNodeKeyHash = (mask << shift) | keyHash;

            if ((nodeMap & bitpos) != 0) { // node (not value)
                // Print the sub-node
                final Node<K, V> subNode = node.nodeAt(bitpos);
                dumpSubTrie(a, subNode, entryLength, printValue, printSequenceNumber, shift + Node.BIT_PARTITION_SIZE, subNodeKeyHash);

                // Print an arrow to the sub-node
                a.append('n');
                a.append(id);
                a.append(":f");
                a.append(Integer.toString(mask));
                a.append(" -> n");
                a.append(toNodeId(subNodeKeyHash, shift + Node.BIT_PARTITION_SIZE));
                a.append(" [label=\"");
                a.append(toArrowId(mask, shift));
                a.append("\"];\n");
            }
        }
    }

    private void dumpHashCollisionNodeSubTree(@NonNull Appendable a, @NonNull HashCollisionNode<K, V> node, int entryLength, boolean printValue, boolean printSequenceNumber, int shift, int keyHash) throws IOException {
        // Print the node as a record
        a.append("n").append(toNodeId(keyHash, shift));
        a.append(" [color=red;label=\"");
        boolean first = true;

        @NonNull Object[] nodes = node.entries;
        for (int i = 0, index = 0; i < nodes.length; i += entryLength, index++) {
            if (first) {
                first = false;
            } else {
                a.append('|');
            }
            a.append("<f");
            a.append(Integer.toString(index));
            a.append('>');
            a.append(Objects.toString(nodes[i]));
            if (printValue) {
                a.append('=');
                a.append(Objects.toString(nodes[i + 1]));
            }
            if (printSequenceNumber) {
                a.append(" #");
                a.append(((Integer) nodes[i + entryLength - 1]).toString());
            }
        }
        a.append("\"];\n");
    }

    private void dumpSubTrie(@NonNull Appendable a, Node<K, V> node, int entryLength, boolean printValue, boolean printSequenceNumber, int shift, int keyHash) throws IOException {
        if (node instanceof BitmapIndexedNode) {
            dumpBitmapIndexedNodeSubTree(a, (BitmapIndexedNode<K, V>) node,
                    entryLength, printValue, printSequenceNumber, shift, keyHash);
        } else {
            dumpHashCollisionNodeSubTree(a, (HashCollisionNode<K, V>) node,
                    entryLength, printValue, printSequenceNumber, shift, keyHash);

        }

    }

    /**
     * Dumps a CHAMP Trie in the Graphviz DOT language.
     *
     * @param a                   an {@link Appendable}
     * @param root                the root node of the trie
     * @param entryLength         the entry length
     * @param printValue          whether to print the value of an entry
     * @param printSequenceNumber whether to print the sequence number of an entry
     */
    public void dumpTrie(@NonNull Appendable a, Node<K, V> root, int entryLength, boolean printValue, boolean printSequenceNumber) throws IOException {
        a.append("digraph ChampTrie {\n");
        a.append("node [shape=record];\n");
        dumpSubTrie(a, root, entryLength, printValue, printSequenceNumber, 0, 0);
        a.append("}\n");
    }

    /**
     * Dumps a CHAMP Trie in the Graphviz DOT language.
     *
     * @param root                the root node of the trie
     * @param entryLength         the entry length
     * @param printValue          whether to print the value of an entry
     * @param printSequenceNumber whether to print the sequence number of an entry
     * @return the dumped trie
     */
    public @NonNull String dumpTrie(Node<K, V> root, int entryLength, boolean printValue, boolean printSequenceNumber) {
        StringBuilder a = new StringBuilder();
        try {
            dumpTrie(a, root, entryLength, printValue, printSequenceNumber);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return a.toString();
    }

    private @NonNull String toArrowId(int mask, int shift) {
        String id = Integer.toBinaryString((mask) & Node.BIT_PARTITION_MASK);
        StringBuilder buf = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = id.length(); i < min(Node.HASH_CODE_LENGTH - shift, Node.BIT_PARTITION_SIZE); i++) {
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
