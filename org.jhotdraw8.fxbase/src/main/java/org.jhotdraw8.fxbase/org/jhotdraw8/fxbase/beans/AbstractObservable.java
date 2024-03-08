/*
 * @(#)AbstractObservable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.InvalidationListener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractObservable.
 *
 * @author Werner Randelshofer
 */
public class AbstractObservable implements ObservableMixin {

    private CopyOnWriteArrayList<InvalidationListener> invalidationListeners;

    public AbstractObservable() {
    }

    @Override
    public CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        if (invalidationListeners == null) {
            invalidationListeners = new CopyOnWriteArrayList<>();
        }
        return invalidationListeners;
    }

}
