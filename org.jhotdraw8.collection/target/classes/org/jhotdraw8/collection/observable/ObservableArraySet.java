/*
 * @(#)ObservableArraySet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.observable;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.ArrayList;

/**
 * An observable set that is backed by an array list
 *
 * @param <E> the element type
 */
public class ObservableArraySet<E> extends ArrayList<E> implements ObservableSet<E> {
    private static final long serialVersionUID = 1L;

    public ObservableArraySet() {
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listener) {
        throw new UnsupportedOperationException();
    }
}
