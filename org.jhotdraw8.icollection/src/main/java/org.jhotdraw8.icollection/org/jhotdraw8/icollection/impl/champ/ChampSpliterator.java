/*
 * @(#)KeySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jspecify.annotations.Nullable;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/// Data iterator over a CHAMP trie.
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
public class ChampSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
    private final Function<Object, E> mappingFunction;
    private static final int MAX_DEPTH = 7;

    protected int currentValueCursor;
    protected int currentValueLength;
    protected Node currentValueNode;

    private int currentStackLevel = -1;

    /// Even indexes: node index
    /// Odd indexes: node length
    private final int[] indexAndArity = new int[MAX_DEPTH * 2];

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantSuppression"})
    final Node[] nodes = new Node[MAX_DEPTH];
    private Object current;
    private final int ENTRY_LENGTH;
    private final int DATA_INDEX;

    @SuppressWarnings("unchecked")
    public ChampSpliterator(Node rootNode, @Nullable Function<Object, E> mappingFunction, long size, int characteristics, int entryLength, int dataIndex) {
        super(size, characteristics);
        this.mappingFunction = mappingFunction == null ? k -> (E) k : mappingFunction;
        ENTRY_LENGTH = entryLength;
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

    private boolean searchNextValueNode() {
        // For inlining, it is essential that this method has a very small amount of byte code!
        while (currentStackLevel >= 0) {
            var index = currentStackLevel << 1;
            if (indexAndArity[index] < indexAndArity[index + 1]) {
                var nextNode = nodes[currentStackLevel].getNode(indexAndArity[index]);
                indexAndArity[index]++;
                if (nextNode.hasNodes()) {
                    ++currentStackLevel;
                    index += 2;
                    nodes[currentStackLevel] = nextNode;
                    indexAndArity[index] = 0;
                    indexAndArity[index + 1] = nextNode.nodeArity();
                }
                if (nextNode.hasData()) {
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

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        // For inlining, it is essential that this method has a very small amount of byte code!
        // Specifically, do not inline searchNextValueNode() into this method!
        if (currentValueCursor < currentValueLength || searchNextValueNode()) {
            action.accept(mappingFunction.apply(currentValueNode.getData(currentValueCursor++, ENTRY_LENGTH, DATA_INDEX)));
            return true;
        }
        return false;
    }
}
