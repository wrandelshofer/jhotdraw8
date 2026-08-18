/*
 * @(#)BaseTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import java.util.Map;

/// Entry iterator over a CHAMP trie.
///
/// Uses a fixed stack in depth.
/// Iterates first over inlined data entries and then continues depth first.
///
/// Supports remove and [Map.Entry#setValue]. The functions that are
/// passed to this iterator must not change the trie structure that the iterator
/// currently uses.
public class AbstractChampIterator {

    private final int[] nodeCursorsAndLengths = new int[Node.MAX_DEPTH * 2];
    protected int nextValueCursor;
    private int nextValueLength;
    private int nextStackLevel = -1;
    protected Node nextValueNode;
    protected boolean canRemove = false;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final Node[] nodes = new Node[Node.MAX_DEPTH];
    protected final int ENTRY_LENGTH;

    /// Creates a new instance.
    ///
    /// @param rootNode     the root node of the trie
    /// @param ENTRY_LENGTH
    public AbstractChampIterator(Node rootNode, int ENTRY_LENGTH) {
        this.ENTRY_LENGTH = ENTRY_LENGTH;

        if (rootNode.hasNodes()) {
            nextStackLevel = 0;
            nodes[0] = rootNode;
            nodeCursorsAndLengths[0] = 0;
            nodeCursorsAndLengths[1] = rootNode.nodeArity();
        }
        if (rootNode.hasData()) {
            nextValueNode = rootNode;
            nextValueCursor = 0;
            nextValueLength = rootNode.dataArity(ENTRY_LENGTH);
        }
    }

    public boolean hasNext() {
        if (nextValueCursor < nextValueLength) {
            return true;
        } else {
            return searchNextValueNode();
        }
    }


    /*
     * Searches for the next node that contains values.
     */
    private boolean searchNextValueNode() {
        while (nextStackLevel >= 0) {
            int currentCursorIndex = nextStackLevel * 2;
            int currentLengthIndex = currentCursorIndex + 1;
            int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
            int nodeLength = nodeCursorsAndLengths[currentLengthIndex];
            if (nodeCursor < nodeLength) {
                Node nextNode = nodes[nextStackLevel].getNode(nodeCursor);
                nodeCursorsAndLengths[currentCursorIndex]++;
                if (nextNode.hasNodes()) {
                    // put node on next stack level for depth-first traversal
                    int nextStackLevel = ++this.nextStackLevel;
                    int nextCursorIndex = nextStackLevel * 2;
                    int nextLengthIndex = nextCursorIndex + 1;
                    nodes[nextStackLevel] = nextNode;
                    nodeCursorsAndLengths[nextCursorIndex] = 0;
                    nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                }
                if (nextNode.hasData()) {
                    //found next node that contains values
                    nextValueNode = nextNode;
                    nextValueCursor = 0;
                    nextValueLength = nextNode.dataArity(ENTRY_LENGTH);
                    return true;
                }
            } else {
                nextStackLevel--;
            }
        }
        return false;
    }


}
