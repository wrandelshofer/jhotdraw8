package org.jhotdraw8.icollection.impl.champset;


/// This code has been derived from
/// [kotlix.collections.immutable, PersistentHashSetIterator.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableSet/PersistentHashSetIterator.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class TrieNodeIterator<E> {
    private Object[] buffer = TrieNode.EMPTY.buffer;
    private int index = 0;

    public TrieNodeIterator() {
    }

    public void reset(Object[] buffer, int index) {
        this.buffer = buffer;
        this.index = index;
    }

    boolean hasNextCell() {
        return index < buffer.length;
    }

    void moveToNextCell() {
        assert hasNextCell();
        index++;
    }

    boolean hasNextElement() {
        return hasNextCell() && !(buffer[index] instanceof TrieNode<?>);
    }

    @SuppressWarnings("unchecked")
    E currentElement() {
        assert hasNextElement();
        return (E) buffer[index];
    }

    @SuppressWarnings("unchecked")
    E nextElement() {
        assert hasNextElement();
        return (E) buffer[index++];
    }

    boolean hasNextNode() {
        return hasNextCell() && buffer[index] instanceof TrieNode<?>;
    }

    @SuppressWarnings("unchecked")
    TrieNode<E> currentNode() {
        assert hasNextNode();
        return (TrieNode<E>) buffer[index];
    }
}
