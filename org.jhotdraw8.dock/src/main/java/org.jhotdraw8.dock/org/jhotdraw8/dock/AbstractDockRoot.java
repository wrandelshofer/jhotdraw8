/*
 * @(#)AbstractDockRoot.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.dock;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jhotdraw8.annotation.NonNull;

import java.util.function.Predicate;

/**
 * Abstract base class for implementations of {@link DockRoot}.
 */
public abstract class AbstractDockRoot
        extends AbstractDockParent
        implements DockRoot {

    private final @NonNull ObjectProperty<Predicate<Dockable>> dockablePredicate = new SimpleObjectProperty<>(d -> true);

    public AbstractDockRoot() {
    }

    @Override
    public @NonNull ObjectProperty<Predicate<Dockable>> dockablePredicateProperty() {
        return dockablePredicate;
    }
}
