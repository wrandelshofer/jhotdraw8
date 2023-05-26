package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * CHAMP Trie iterator.
 * <p>
 * References:
 * <p>
 * This class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl> *
 *
 * @param <K> the data type of the trie node
 * @param <E> the element type of the iterator
 */
public class ChampIterator<K, E> implements Iterator<E> {
    private final @NonNull Function<K, E> mappingFunction;
    private static final int MAX_DEPTH = 7;

    protected int currentValueCursor;
    protected int currentValueLength;
    protected Node<K> currentValueNode;

    private int currentStackLevel = -1;
    private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];

    @SuppressWarnings("unchecked")
    Node<K>[] nodes = new Node[MAX_DEPTH];

    public ChampIterator(@NonNull Node<K> rootNode, @Nullable Function<K, E> mappingFunction) {
        this.mappingFunction = mappingFunction == null ? k -> (E) k : mappingFunction;
        if (rootNode.hasNodes()) {
            currentStackLevel = 0;

            nodes[0] = rootNode;
            nodeCursorsAndLengths[0] = 0;
            nodeCursorsAndLengths[1] = rootNode.nodeArity();
        }

        if (rootNode.hasData()) {
            currentValueNode = rootNode;
            currentValueCursor = 0;
            currentValueLength = rootNode.dataArity();
        }
    }

    /*
     * search for next node that contains values
     */
    private boolean searchNextValueNode() {
        while (currentStackLevel >= 0) {
            var currentCursorIndex = currentStackLevel * 2;
            var currentLengthIndex = currentCursorIndex + 1;
            var nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
            var nodeLength = nodeCursorsAndLengths[currentLengthIndex];
            if (nodeCursor < nodeLength) {
                var nextNode = nodes[currentStackLevel].getNode(nodeCursor);
                nodeCursorsAndLengths[currentCursorIndex]++;
                if (nextNode.hasNodes()) {
                    // put node on next stack level for depth-first traversal
                    var nextStackLevel = ++currentStackLevel;
                    var nextCursorIndex = nextStackLevel * 2;
                    var nextLengthIndex = nextCursorIndex + 1;
                    nodes[nextStackLevel] = nextNode;
                    nodeCursorsAndLengths[nextCursorIndex] = 0;
                    nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                }
                if (nextNode.hasData()) {
                    // found next node that contains values
                    currentValueNode = nextNode;
                    currentValueCursor = 0;
                    currentValueLength = nextNode.dataArity();
                    return true;
                }
            } else {
                currentStackLevel--;
            }
        }

        return false;
    }

    public boolean hasNext() {
        return currentValueCursor < currentValueLength || searchNextValueNode();
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            return mappingFunction.apply(currentValueNode.getData(currentValueCursor++));
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
