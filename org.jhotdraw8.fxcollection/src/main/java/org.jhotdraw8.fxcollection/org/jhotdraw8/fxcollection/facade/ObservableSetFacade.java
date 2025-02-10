/*
 * @(#)ObservableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.facade;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.readable.ReadableSet;

/**
 * Wraps a {@link ReadableSet} in the {@link ObservableSet} interface.
 * <p>
 * The underlying ReadableSet is referenced - not copied. This allows to pass a
 * ReadableSet to a client who does not understand the ReadableSet APi.
 *
 * @param <E> the element type
 */
public class ObservableSetFacade<E> extends SetFacade<E> implements ObservableSet<E> {
    public ObservableSetFacade(ReadableSet<E> backingSet) {
        super(backingSet);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener) {
        // empty
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listener) {
        // empty
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
