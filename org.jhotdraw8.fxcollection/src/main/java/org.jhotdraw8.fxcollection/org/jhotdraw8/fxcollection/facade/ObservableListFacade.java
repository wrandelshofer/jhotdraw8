/*
 * @(#)ObservableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.facade;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jhotdraw8.icollection.facade.ListFacade;
import org.jhotdraw8.icollection.readable.ReadableList;

import java.util.Collection;

/**
 * Wraps a {@link ReadableList} in the {@link ObservableList} interface.
 * <p>
 * The underlying ReadableList is referenced - not copied. This allows to pass a
 * ReadableList to a client who does not understand the ReadableList APi.
 *
 * @param <E> the element type
 */
public class ObservableListFacade<E> extends ListFacade<E> implements ObservableList<E> {
    public ObservableListFacade(ReadableList<E> backingList) {
        super(backingList);
    }

    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        // empty
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        // empty
    }

    @SafeVarargs
    @Override
    public final boolean addAll(E... elements) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final boolean setAll(E... elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final boolean removeAll(E... elements) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final boolean retainAll(E... elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        // empty
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        // empty
    }
}
