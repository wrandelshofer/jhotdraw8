/*
 * @(#)ChampTrieGraphviz.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static java.lang.Math.min;

/// Dumps a CHAMP trie in the Graphviz DOT language.
///
/// References:
/// <dl>
///     <dt>Graphviz. DOT Language.</dt>
///     <dd><a href="https://graphviz.org/doc/info/lang.html">graphviz.org</a></dd>
/// </dl>
///
/// @param <K> the key type
/// @param <V> the value type
public class ChampTrieGraphviz<K, V> {

    private void dumpBitmapIndexedNodeSubTree(
            Appendable a, BitmapIndexedNode node, int entryLength, boolean printValue,
            boolean printSequenceNumber, int shift, int keyHash, int ENTRY_LENGTH) throws IOException {

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
                    a.append(Objects.toString(node.getKey(Node.index(dataMap, bitpos), ENTRY_LENGTH)));
                    if (printValue) {
                        a.append('=');
                        a.append(Objects.toString(node.getData(Node.index(dataMap, bitpos), ENTRY_LENGTH)));
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
                Node subNode = node.nodeAt(bitpos);
                dumpSubTrie(a, subNode, entryLength, printValue, printSequenceNumber, shift + Node.BIT_PARTITION_SIZE, subNodeKeyHash, ENTRY_LENGTH);

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

    private void dumpHashCollisionNodeSubTree(Appendable a, HashCollisionNode node, int entryLength, boolean printValue, boolean printSequenceNumber, int shift, int keyHash) throws IOException {
        // Print the node as a record
        a.append("n").append(toNodeId(keyHash, shift));
        a.append(" [color=red;label=\"");
        boolean first = true;

        Object[] nodes = node.array;
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

    private void dumpSubTrie(Appendable a, Node node, int entryLength, boolean printValue, boolean printSequenceNumber, int shift, int keyHash, int ENTRY_LENGTH) throws IOException {
        if (node instanceof BitmapIndexedNode) {
            dumpBitmapIndexedNodeSubTree(a, (BitmapIndexedNode) node,
                    entryLength, printValue, printSequenceNumber, shift, keyHash, ENTRY_LENGTH);
        } else {
            dumpHashCollisionNodeSubTree(a, (HashCollisionNode) node,
                    entryLength, printValue, printSequenceNumber, shift, keyHash);

        }

    }

    /// Dumps a CHAMP Trie in the Graphviz DOT language.
    ///
    /// @param a                   an [Appendable]
    /// @param root                the root node of the trie
    /// @param entryLength         the entry length
    /// @param printValue          whether to print the value of an entry
    /// @param printSequenceNumber whether to print the sequence number of an entry
    /// @param ENTRY_LENGTH
    public void dumpTrie(Appendable a, Node root, int entryLength, boolean printValue, boolean printSequenceNumber, int ENTRY_LENGTH) throws IOException {
        a.append("digraph ChampTrie {\n");
        a.append("node [shape=record];\n");
        dumpSubTrie(a, root, entryLength, printValue, printSequenceNumber, 0, 0, ENTRY_LENGTH);
        a.append("}\n");
    }

    /// Dumps a CHAMP Trie in the Graphviz DOT language.
    ///
    /// @param root                the root node of the trie
    /// @param entryLength         the entry length
    /// @param printValue          whether to print the value of an entry
    /// @param printSequenceNumber whether to print the sequence number of an entry
    /// @param ENTRY_LENGTH
    /// @return the dumped trie
    public String dumpTrie(Node root, int entryLength, boolean printValue, boolean printSequenceNumber, int ENTRY_LENGTH) {
        StringBuilder a = new StringBuilder();
        try {
            dumpTrie(a, root, entryLength, printValue, printSequenceNumber, ENTRY_LENGTH);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return a.toString();
    }

    private String toArrowId(int mask, int shift) {
        String id = Integer.toBinaryString((mask) & Node.BIT_PARTITION_MASK);
        StringBuilder buf = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = id.length(); i < min(Node.HASH_CODE_LENGTH - shift, Node.BIT_PARTITION_SIZE); i++) {
            buf.append('0');
        }
        buf.append(id);
        return buf.toString();
    }

    private String toNodeId(int keyHash, int shift) {
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
