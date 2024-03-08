/*
 * @(#)BaseTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Entry iterator over a CHAMP trie.
 * <p>
 * Uses a fixed stack in depth.
 * Iterates first over inlined data entries and then continues depth first.
 * <p>
 * Supports remove and {@link Map.Entry#setValue}. The functions that are
 * passed to this iterator must not change the trie structure that the iterator
 * currently uses.
 */
public class EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final int[] nodeCursorsAndLengths = new int[Node.MAX_DEPTH * 2];
    int nextValueCursor;
    private int nextValueLength;
    private int nextStackLevel = -1;
    Node<K, V> nextValueNode;
    @Nullable EditableMapEntry<K, V> current;
    private boolean canRemove = false;
    private final @Nullable Consumer<K> persistentRemoveFunction;
    private final @Nullable BiConsumer<K, V> persistentPutIfPresentFunction;
    @SuppressWarnings({"unchecked", "rawtypes"})
    final Node<K, V> @NonNull [] nodes = new Node[Node.MAX_DEPTH];

    /**
     * Creates a new instance.
     *
     * @param rootNode                       the root node of the trie
     * @param persistentRemoveFunction       a function that removes an entry from a field;
     *                                       the function must not change the trie that was passed
     *                                       to this iterator
     * @param persistentPutIfPresentFunction a function that replaces the value of an entry;
     *                                       the function must not change the trie that was passed
     *                                       to this iterator
     */
    public EntryIterator(@NonNull Node<K, V> rootNode, @Nullable Consumer<K> persistentRemoveFunction, @Nullable BiConsumer<K, V> persistentPutIfPresentFunction) {
        this.persistentRemoveFunction = persistentRemoveFunction;
        this.persistentPutIfPresentFunction = persistentPutIfPresentFunction;
        if (rootNode.hasNodes()) {
            nextStackLevel = 0;
            nodes[0] = rootNode;
            nodeCursorsAndLengths[0] = 0;
            nodeCursorsAndLengths[1] = rootNode.nodeArity();
        }
        if (rootNode.hasData()) {
            nextValueNode = rootNode;
            nextValueCursor = 0;
            nextValueLength = rootNode.dataArity();
        }
    }

    @Override
    public boolean hasNext() {
        if (nextValueCursor < nextValueLength) {
            return true;
        } else {
            return searchNextValueNode();
        }
    }

    @Override
    public @Nullable EditableMapEntry<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            canRemove = true;
            current = nextValueNode.getMapEntry(nextValueCursor++);
            current.setPutIfPresentFunction(persistentPutIfPresentFunction);
            return current;
        }
    }

    /*
     * Searches for the next node that contains values.
     */
    private boolean searchNextValueNode() {
        while (nextStackLevel >= 0) {
            final int currentCursorIndex = nextStackLevel * 2;
            final int currentLengthIndex = currentCursorIndex + 1;
            final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
            final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];
            if (nodeCursor < nodeLength) {
                final Node<K, V> nextNode = nodes[nextStackLevel].getNode(nodeCursor);
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
                    nextValueLength = nextNode.dataArity();
                    return true;
                }
            } else {
                nextStackLevel--;
            }
        }
        return false;
    }

    @Override
    public void remove() {
        if (persistentRemoveFunction == null) {
            throw new UnsupportedOperationException("remove");
        }
        if (!canRemove || current == null) {
            throw new IllegalStateException();
        }
        Map.Entry<K, V> toRemove = current;
        persistentRemoveFunction.accept(toRemove.getKey());
        canRemove = false;
        current = null;
    }
}
