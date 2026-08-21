package org.jhotdraw8.icollection.impl.champlinked;


import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public class LinkedElementIterator<E> implements Iterator<E> {
    private @Nullable Object @Nullable [] current;
    private TrieNode<E> root;
    private final Function<Object[], E> mapper;
    private int ENTRY_SIZE;
    private int NEXT_DATA_INDEX;


    public LinkedElementIterator(TrieNode<E> root, Object @Nullable [] first, Function<Object[], E> mapper, int ENTRY_SIZE, int nextDataIndex) {
        this.root = root;
        this.mapper = mapper;
        this.NEXT_DATA_INDEX = nextDataIndex;
        this.ENTRY_SIZE = ENTRY_SIZE;
        if (first != null) {
            this.current = first.clone();
        }
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @SuppressWarnings({"NullableProblems", "DataFlowIssue", "unchecked"})
    @Override
    public E next() {
        if (current == null) {
            throw new NoSuchElementException();
        }
        E value = mapper.apply(current);
        if (current[NEXT_DATA_INDEX] == TrieNode.NO_DATA) {
            current = null;
        } else {
            Object key = current[NEXT_DATA_INDEX];
            boolean success = root.getArrayEntry(Objects.hashCode(key), (E) key, 0, current, ENTRY_SIZE);
            if (!success) {
                current = null;
            }
        }
        return value;
    }

}
