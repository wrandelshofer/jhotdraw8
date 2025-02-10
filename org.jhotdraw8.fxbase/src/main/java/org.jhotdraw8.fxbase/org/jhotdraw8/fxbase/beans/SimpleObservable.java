/*
 * @(#)SimpleObservable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.InvalidationListener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SimpleObservable.
 *
 */
public class SimpleObservable implements ObservableMixin {

    private final CopyOnWriteArrayList<InvalidationListener> invalidationListeners = new CopyOnWriteArrayList<>();

    public SimpleObservable() {
    }

    @Override
    public CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        return invalidationListeners;
    }
}
