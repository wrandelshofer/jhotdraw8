/*
 * @(#)IndexedArrayObservableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcollection.indexedset;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A set that provides precise control where each element is inserted;
 * this set is backed by an array.
 *
 * @param <E> the element type
 */
public class IndexedArrayObservableSet<E> extends AbstractIndexedArrayObservableSet<E> {
    public IndexedArrayObservableSet() {
    }

    public IndexedArrayObservableSet(Collection<? extends E> col) {
        super(col);
    }

    @Override
    protected void onRemoved(E e) {
        // empty
    }

    @Override
    protected void onAdded(E e) {
        // empty
    }

    @Override
    protected @Nullable Boolean onContains(E e) {
        // we do not have a fast implementation
        return null;
    }

    @Override
    protected boolean mayBeAdded(E e) {
        return true;
    }
}
