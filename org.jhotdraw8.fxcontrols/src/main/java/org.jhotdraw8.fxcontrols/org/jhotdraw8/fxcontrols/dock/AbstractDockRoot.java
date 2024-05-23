/*
 * @(#)AbstractDockRoot.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Predicate;

/**
 * Abstract base class for implementations of {@link DockRoot}.
 */
public abstract class AbstractDockRoot
        extends AbstractDockParent
        implements DockRoot {

    private final ObjectProperty<Predicate<Dockable>> dockablePredicate = new SimpleObjectProperty<>(d -> true);

    public AbstractDockRoot() {
    }

    @Override
    public ObjectProperty<Predicate<Dockable>> dockablePredicateProperty() {
        return dockablePredicate;
    }
}
