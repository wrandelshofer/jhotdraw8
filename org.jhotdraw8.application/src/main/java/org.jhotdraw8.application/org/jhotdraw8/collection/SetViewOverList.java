/*
 * @(#)SetViewOverList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable set view over a list.
 *
 * @param <E> the element type
 */
public class SetViewOverList<E> extends AbstractSet<E> {
    private final @NonNull List<E> list;

    public SetViewOverList(final @NonNull List<E> list) {
        this.list = list;
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }
}
