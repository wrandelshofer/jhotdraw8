package org.jhotdraw8.icollection.impl.linked;


import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public class LinkedElementIterator<E> implements Iterator<E> {
    private @Nullable Object[] current;
    private BitmapIndexedNode root;
    private final Function<Object[], E> mapper;
    private int DATA_LENGTH;
    private int NEXT_DATA_INDEX;


    public LinkedElementIterator(Object[] current, BitmapIndexedNode root, Function<Object[], E> mapper, int dataLength, int nextDataIndex) {
        this.current = current;
        this.root = root;
        this.mapper = mapper;
        this.NEXT_DATA_INDEX = nextDataIndex;
        this.DATA_LENGTH = dataLength;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public E next() {
        if (current == null) {
            throw new NoSuchElementException();
        }
        E value = mapper.apply(current);
        current = current[NEXT_DATA_INDEX] == org.jhotdraw8.icollection.impl.champ.Node.NO_DATA ? null : get(current[NEXT_DATA_INDEX]);
        return value;
    }

    private @Nullable Object[] get(@Nullable Object o) {
        if (o == null) {
            return null;
        }
        Object result = root.findData(o, Objects.hashCode(o), 0, DATA_LENGTH);
        return result == org.jhotdraw8.icollection.impl.champ.Node.NO_DATA ? null : (Object[]) result;
    }
}
