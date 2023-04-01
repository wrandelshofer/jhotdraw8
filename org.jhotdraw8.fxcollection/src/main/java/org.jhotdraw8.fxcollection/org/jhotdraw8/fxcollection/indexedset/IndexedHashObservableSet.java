/*
 * @(#)IndexedHashObservableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.indexedset;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A set that provides precise control where each element is inserted;
 * this set is backed by an array and a hash set.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class IndexedHashObservableSet<E> extends AbstractIndexedArrayObservableSet<E> {
    /**
     * The hash set.
     */
    private final @NonNull Set<E> set = new HashSet<>();

    public IndexedHashObservableSet() {
    }

    public IndexedHashObservableSet(@NonNull Collection<? extends E> col) {
        setAll(col);
    }


    @Override
    protected void onAdded(@NonNull E e) {
        set.add(e);
    }

    @Override
    protected Boolean onContains(E e) {
        return set.contains(e);
    }

    @Override
    protected boolean mayBeAdded(@NonNull E e) {
        return true;
    }

    @Override
    protected void onRemoved(@NonNull E e) {
        set.remove(e);
    }
}
