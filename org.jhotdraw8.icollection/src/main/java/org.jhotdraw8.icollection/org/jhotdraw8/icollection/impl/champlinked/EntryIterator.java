package org.jhotdraw8.icollection.impl.champlinked;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public class EntryIterator<K, V, E> implements Iterator<E> {
    private Object[][] nodeArraysStack = new Object[TrieNode.LOG_MAX_BRANCHING_FACTOR][];
    private int[] nodeCountsStack = new int[TrieNode.LOG_MAX_BRANCHING_FACTOR];
    private int stackDepth = -1;
    private Object[] entries = new Object[0];
    private int entryCount;
    private final int ENTRY_SIZE;
    private final BiFunction<K, V, E> mapper;

    public EntryIterator(TrieNode<K> node, int entrySize, BiFunction<K, V, E> mapper) {
        ENTRY_SIZE = entrySize;
        this.mapper = mapper;
        push(node);
    }


    @Override
    public boolean hasNext() {
        return entryCount > 0 || stackDepth >= 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        while (entryCount == 0) moveToNextNode();
        entryCount--;
        K k = (K) entries[entryCount * ENTRY_SIZE];
        V v = (V) entries[entryCount * ENTRY_SIZE + 1];
        return mapper.apply(k, v);
    }

    @SuppressWarnings("unchecked")
    private void moveToNextNode() {
        int nodeCount = nodeCountsStack[stackDepth]--;
        if (nodeCount > 0) {
            Object[] nodeArray = nodeArraysStack[stackDepth];
            TrieNode<K> node = (TrieNode<K>) nodeArray[nodeArray.length - nodeCount];
            if (nodeCount == 1) pop();
            push(node);
        } else {
            pop();
        }
    }

    private void push(TrieNode<K> node) {
        int nodeCount = node.nodeCount();
        if (nodeCount > 0) {
            stackDepth++;
            nodeArraysStack[stackDepth] = node.buffer;
            nodeCountsStack[stackDepth] = nodeCount;
        }
        this.entries = node.buffer;
        this.entryCount = node.collisionEntryCount(ENTRY_SIZE);
    }

    private void pop() {
        stackDepth--;
    }
}
