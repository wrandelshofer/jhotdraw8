/*
 * @(#)WrappedObservableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.jhotdraw8.annotation.NonNull;

/**
 * Wraps a {@link ReadOnlySet} in the {@link ObservableSet} interface.
 * <p>
 * The underlying ReadOnlySet is referenced - not copied. This allows to pass a
 * ReadOnlySet to a client who does not understand the ReadOnlySet APi.
 *
 * @author Werner Randelshofer
 */
public class WrappedObservableSet<E> extends WrappedSet<E> implements ObservableSet<E> {
    public WrappedObservableSet(@NonNull ReadOnlySet<E> backingSet) {
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
