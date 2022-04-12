/*
 * @(#)ReversedList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A ReversedList provides an unmodifiable view on a List in reverse order.
 *
 * @param <T> the list type
 * @author wrandels
 */
public class ReversedList<T> extends AbstractList<T> {

    private final @NonNull List<T> target;

    /**
     * Creates a new instance of ReversedList.
     *
     * @param target the target list
     */
    public ReversedList(@NonNull Collection<T> target) {
        this.target = (target instanceof List) ? (List<T>) target : new ArrayList<>(target);
    }

    @Override
    public T get(int index) {
        return target.get(target.size() - 1 - index);
    }

    @Override
    public int size() {
        return target.size();
    }

}
