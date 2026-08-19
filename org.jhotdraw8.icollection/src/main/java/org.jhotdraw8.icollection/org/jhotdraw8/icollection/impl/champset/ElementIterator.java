package org.jhotdraw8.icollection.impl.champset;

import java.util.Iterator;
import java.util.NoSuchElementException;

/// This code has been derived from
/// [kotlix.collections.immutable, PersistentHashSetIterator.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableSet/PersistentHashSetIterator.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class ElementIterator<E> implements Iterator<E> {
    TrieNodeIterator<E>[] path = new TrieNodeIterator[TrieNode.MAX_BRANCHING_FACTOR];
    private int pathLength;
    protected int pathLastIndex = 0;

    private boolean hasNext = true;

    public ElementIterator(TrieNode<E> node) {
        TrieNodeIterator<E> e = new TrieNodeIterator<>();
        e.reset(node.buffer, 0);
        path[0] = e;
        pathLength = 1;
        pathLastIndex = 0;
        ensureNextElementIsReady();
    }

    private int moveToNextNodeWithData(int pathIndex) {
        if (path[pathIndex].hasNextElement()) {
            return pathIndex;
        }
        if (path[pathIndex].hasNextNode()) {
            var node = path[pathIndex].currentNode();

            if (pathIndex + 1 == pathLength) {
                pathAdd(new TrieNodeIterator<>());
            }
            path[pathIndex + 1].reset(node.buffer, 0);
            return moveToNextNodeWithData(pathIndex + 1);
        }
        return -1;
    }

    private void pathAdd(TrieNodeIterator<E> objectTrieNodeIterator) {
        path[pathLength++] = objectTrieNodeIterator;
    }

    private void ensureNextElementIsReady() {
        if (path[pathLastIndex].hasNextElement()) {
            return;
        }
        for (int i = pathLastIndex; i >= 0; i--) {
            var result = moveToNextNodeWithData(i);

            if (result == -1 && path[i].hasNextCell()) {
                path[i].moveToNextCell();
                result = moveToNextNodeWithData(i);
            }
            if (result != -1) {
                pathLastIndex = result;
                return;
            }
            if (i > 0) {
                path[i - 1].moveToNextCell();
            }
            path[i].reset(TrieNode.EMPTY.buffer, 0);
        }
        hasNext = false;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public E next() {
        if (!hasNext)
            throw new NoSuchElementException();

        var result = path[pathLastIndex].nextElement();
        ensureNextElementIsReady();
        return result;
    }

    protected E currentElement() {
        assert hasNext();
        return path[pathLastIndex].currentElement();
    }
}
