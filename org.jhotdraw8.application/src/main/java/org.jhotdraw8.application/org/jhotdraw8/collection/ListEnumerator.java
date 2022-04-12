/*
 * @(#)ListEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.List;
import java.util.Spliterator;

/**
 * {@link Enumerator} and {@link Spliterator} for a {@link List}.
 * <p>
 * Does not perform modification checks of the list.
 *
 * @param <T> the element type of the list
 */
public class ListEnumerator<T> extends AbstractEnumerator<T> {
    private int index;
    private int endIndex;
    private final List<T> list;

    public ListEnumerator(List<T> list) {
        this(list, 0, list.size());
    }

    public ListEnumerator(List<T> list, int startIndex, int endIndex) {
        super(endIndex - startIndex, Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
        index = startIndex;
        this.endIndex = endIndex;
        this.list = list;
    }

    @Override
    public boolean moveNext() {
        if (index < endIndex) {
            current = list.get(index++);
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        int hi = endIndex, lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new ListEnumerator<>(list, lo, index = mid);
    }
}
