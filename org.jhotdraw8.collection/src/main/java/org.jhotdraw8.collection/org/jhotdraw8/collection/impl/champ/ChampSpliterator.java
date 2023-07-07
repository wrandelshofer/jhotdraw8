/*
 * @(#)KeySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Data iterator over a CHAMP trie.
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
public class ChampSpliterator<K, E> extends Spliterators.AbstractSpliterator<E> {
    private final @NonNull Function<K, E> mappingFunction;
    private static final int MAX_DEPTH = 7;

    protected int currentValueCursor;
    protected int currentValueLength;
    protected Node<K> currentValueNode;

    private int currentStackLevel = -1;

    /**
     * Even indexes: node index
     * Odd indexes: node length
     */
    private final int[] indexAndArity = new int[MAX_DEPTH * 2];

    @SuppressWarnings("unchecked")
    Node<K>[] nodes = new Node[MAX_DEPTH];
    private K current;

    @SuppressWarnings("unchecked")
    public ChampSpliterator(@NonNull Node<K> rootNode, @Nullable Function<K, E> mappingFunction, long size, int characteristics) {
        super(size, characteristics);
        this.mappingFunction = mappingFunction == null ? k -> (E) k : mappingFunction;
        if (rootNode.hasNodes()) {
            currentStackLevel = 0;

            nodes[0] = rootNode;
            indexAndArity[0] = 0;
            indexAndArity[1] = rootNode.nodeArity();
        }

        if (rootNode.hasData()) {
            currentValueNode = rootNode;
            currentValueCursor = 0;
            currentValueLength = rootNode.dataArity();
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
                    currentValueLength = nextNode.dataArity();
                    return true;
                }
            } else {
                currentStackLevel--;
            }
        }
        return false;
    }

    @Override
    public boolean tryAdvance(@NonNull Consumer<? super E> action) {
        // For inlining, it is essential that this method has a very small amount of byte code!
        // Specifically, do not inline searchNextValueNode() into this method!
        if (currentValueCursor < currentValueLength || searchNextValueNode()) {
            action.accept(mappingFunction.apply(currentValueNode.getData(currentValueCursor++)));
            return true;
        }
        return false;
    }
}
