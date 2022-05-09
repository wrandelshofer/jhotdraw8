/*
 * @(#)SimpleObservable.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.beans;

import javafx.beans.InvalidationListener;
import org.jhotdraw8.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SimpleObservable.
 *
 * @author Werner Randelshofer
 */
public class SimpleObservable implements ObservableMixin {

    private final CopyOnWriteArrayList<InvalidationListener> invalidationListeners = new CopyOnWriteArrayList<>();

    public SimpleObservable() {
    }

    @Override
    public @NonNull CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners() {
        return invalidationListeners;
    }
}
