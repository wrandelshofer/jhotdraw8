package org.jhotdraw8.icollection.impl.champ;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/// CHAMP Trie iterator.
///
/// References:
///
/// This class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
/// </dl> *
///
/// @param <K> the data type of the trie node
/// @param <E> the element type of the iterator
public class ChampIterator<E> implements Iterator<E> {
    private final Function<Object, E> mappingFunction;
    private static final int MAX_DEPTH = 7;

    protected int currentValueCursor;
    protected int currentValueLength;
    protected Node currentValueNode;

    private int currentStackLevel = -1;
    private final int[] indexAndArity = new int[MAX_DEPTH * 2];

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantSuppression"})
    final Node[] nodes = new Node[MAX_DEPTH];
    private final int ENTRY_LENGTH;
    private final int DATA_INDEX;

    @SuppressWarnings("unchecked")
    public ChampIterator(Node rootNode, @Nullable Function<Object, E> mappingFunction, int ENTRY_LENGTH, int dataIndex) {
        this.ENTRY_LENGTH = ENTRY_LENGTH;
        this.mappingFunction = mappingFunction == null ? k -> (E) k : mappingFunction;
        DATA_INDEX = dataIndex;
        if (rootNode.hasNodes()) {
            currentStackLevel = 0;

            nodes[0] = rootNode;
            indexAndArity[0] = 0;
            indexAndArity[1] = rootNode.nodeArity();
        }

        if (rootNode.hasData()) {
            currentValueNode = rootNode;
            currentValueCursor = 0;
            currentValueLength = rootNode.dataArity(ENTRY_LENGTH);
        }
    }

    /*
     * search for next node that contains values
     */
    private boolean searchNextValueNode() {
        // For inlining, it is essential that this method has a very small amount of byte code!
        while (currentStackLevel >= 0) {
            var index = currentStackLevel << 1;
            if (indexAndArity[index] < indexAndArity[index + 1]) {
                var nextNode = nodes[currentStackLevel].getNode(indexAndArity[index]);
                indexAndArity[index]++;
                if (nextNode.hasNodes()) {
                    // put node on next stack level for depth-first traversal
                    ++currentStackLevel;
                    index += 2;
                    nodes[currentStackLevel] = nextNode;
                    indexAndArity[index] = 0;
                    indexAndArity[index + 1] = nextNode.nodeArity();
                }
                if (nextNode.hasData()) {
                    // found next node that contains values
                    currentValueNode = nextNode;
                    currentValueCursor = 0;
                    currentValueLength = nextNode.dataArity(ENTRY_LENGTH);
                    return true;
                }
            } else {
                currentStackLevel--;
            }
        }
        return false;
    }

    public boolean hasNext() {
        // For inlining, it is essential that this method has a very small amount of byte code!
        // Specifically, do not inline searchNextValueNode() into this method!
        return currentValueCursor < currentValueLength || searchNextValueNode();
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            return mappingFunction.apply(currentValueNode.getData(currentValueCursor++, ENTRY_LENGTH, DATA_INDEX));
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
