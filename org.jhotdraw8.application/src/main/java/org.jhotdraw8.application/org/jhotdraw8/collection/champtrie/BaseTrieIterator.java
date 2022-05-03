/*
 * @(#)BaseTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champtrie;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Iterator skeleton that uses a fixed stack in depth.
 * <p>
 * Iterates first over inlined data entrys and then continues depth first.
 */
public class BaseTrieIterator<K, V> {

    final int entryLength;
    private final int[] nodeCursorsAndLengths = new int[Node.MAX_DEPTH * 2];
    int nextValueCursor;
    private int nextValueLength;
    private int nextStackLevel = -1;
    Node<K, V> nextValueNode;
    Map.Entry<K, V> current;
    private boolean canRemove = false;

    @SuppressWarnings({"unchecked"})
    Node<K, V>[] nodes = new Node[Node.MAX_DEPTH];

    protected BaseTrieIterator(Node<K, V> rootNode, int entryLength) {
        this.entryLength = entryLength;
        if (rootNode.hasNodes()) {
            nextStackLevel = 0;

            nodes[0] = rootNode;
            nodeCursorsAndLengths[0] = 0;
            nodeCursorsAndLengths[1] = rootNode.nodeArity();
        }

        if (rootNode.hasData()) {
            nextValueNode = rootNode;
            nextValueCursor = 0;
            nextValueLength = rootNode.dataArity(entryLength);
        }
    }

    public boolean hasNext() {
        if (nextValueCursor < nextValueLength) {
            return true;
        } else {
            return searchNextValueNode(entryLength);
        }
    }

    /**
     * Moves the iterator so that it stands before the specified
     * element.
     *
     * @param k           an element
     * @param rootNode    the root node of the set
     * @param entryLength the entry length
     */
    protected void moveTo(final @Nullable K k, final @NonNull Node<K, V> rootNode, int entryLength) {
        int keyHash = Objects.hashCode(k);
        int shift = 0;
        Node<K, V> node = rootNode;

        nextStackLevel = -1;
        nextValueNode = null;
        nextValueCursor = 0;
        nextValueLength = 0;
        Arrays.fill(nodes, null);
        Arrays.fill(nodeCursorsAndLengths, 0);
        current = null;

        for (int depth = 0; depth < Node.MAX_DEPTH; depth++) {
            nodes[depth] = node;

            int nodeIndex = node.nodeIndex(keyHash, shift);
            int dataIndex = node.dataIndex(k, keyHash, shift, entryLength);
            int nodeArity = node.nodeArity();
            if (nodeArity > 0) {
                // nodeIndex==-1 ? => we need to traverse all child nodes later!
                // nodeIndex!=-1 ? => we must traverse all child nodes after this one
                final int nextCursorIndex = depth * 2;
                final int nextLengthIndex = nextCursorIndex + 1;
                nodeCursorsAndLengths[nextCursorIndex] = nodeIndex + 1;
                nodeCursorsAndLengths[nextLengthIndex] = nodeArity;
            }
            if (nodeIndex == -1) {
                if (dataIndex != -1) {
                    nextValueNode = node;
                    nextValueCursor = dataIndex;
                    nextValueLength = node.dataArity(entryLength);
                    nextStackLevel = depth;
                }
                break;
            } else {
                node = node.getNode(Math.max(nodeIndex, 0), entryLength);

            }

            shift += Node.BIT_PARTITION_SIZE;
        }
    }

    protected Map.Entry<K, V> nextEntry(@NonNull BiFunction<K, V, Map.Entry<K, V>> factory) {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            canRemove = true;
            current = nextValueNode.getKeyValueEntry(nextValueCursor++, factory, entryLength);
            return current;
        }
    }

    protected void removeEntry(Function<K, Node<K, V>> removeFunction) {
        if (!canRemove) {
            throw new IllegalStateException();
        }

        Map.Entry<K, V> toRemove = current;
        if (hasNext()) {
            Map.Entry<K, V> next = nextEntry(AbstractMap.SimpleImmutableEntry::new);
            Node<K, V> newRoot = removeFunction.apply(toRemove.getKey());
            moveTo(next.getKey(), newRoot, entryLength);
        } else {
            removeFunction.apply(toRemove.getKey());
        }

        canRemove = false;
        current = null;
    }

    /*
     * search for next node that contains values
     */
    private boolean searchNextValueNode(int entryLength) {
        while (nextStackLevel >= 0) {
            final int currentCursorIndex = nextStackLevel * 2;
            final int currentLengthIndex = currentCursorIndex + 1;

            final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
            final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

            if (nodeCursor < nodeLength) {
                final Node<K, V> nextNode = nodes[nextStackLevel].getNode(nodeCursor, entryLength);
                nodeCursorsAndLengths[currentCursorIndex]++;

                if (nextNode.hasNodes()) {
                    // put node on next stack level for depth-first traversal
                    final int nextStackLevel = ++this.nextStackLevel;
                    final int nextCursorIndex = nextStackLevel * 2;
                    final int nextLengthIndex = nextCursorIndex + 1;

                    nodes[nextStackLevel] = nextNode;
                    nodeCursorsAndLengths[nextCursorIndex] = 0;
                    nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                }

                if (nextNode.hasData()) {
                    //found next node that contains values
                    nextValueNode = nextNode;
                    nextValueCursor = 0;
                    nextValueLength = nextNode.dataArity(entryLength);
                    return true;
                }
            } else {
                nextStackLevel--;
            }
        }

        return false;
    }
}
