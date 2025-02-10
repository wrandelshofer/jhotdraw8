/*
 * @(#)IndexedHashObservableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.indexedset;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A set that provides precise control where each element is inserted;
 * this set is backed by an array and a hash set.
 *
 * @param <E> the element type
 */
public class IndexedHashObservableSet<E> extends AbstractIndexedArrayObservableSet<E> {
    /**
     * The hash set.
     */
    private final Set<E> set = new HashSet<>();

    public IndexedHashObservableSet() {
    }

    public IndexedHashObservableSet(Collection<? extends E> col) {
        setAll(col);
    }


    @Override
    protected void onAdded(E e) {
        set.add(e);
    }

    @Override
    protected Boolean onContains(E e) {
        return set.contains(e);
    }

    @Override
    protected boolean mayBeAdded(E e) {
        return true;
    }

    @Override
    protected void onRemoved(E e) {
        set.remove(e);
    }
}
