/*
 * @(#)Disableable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.control;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableSet;
import javafx.concurrent.Worker;

/**
 * This interface is used to coordinate user interactions with an object.
 * <p>
 * The user is not allowed to invoke an {@code action} on a disabled object.
 * <p>
 * If the disabled object is a user interface component, then it should
 * visualize that actions may not be invoked. For example by disabling
 * buttons.
 * <p>
 * The object can be disabled by adding {@link Worker}s to its "disabler"
 * set. Additionally the object can disable itself, when its internal
 * state does not permit the invocation of actions.
 * <p>
 * The object is enabled when its "disabler" set is empty and
 * its internal state permits the invocation of actions.
 *
 */
public interface Disableable {

    /**
     * The name of the disabled property.
     */
    String DISABLED_PROPERTY = "disabled";

    /**
     * Indicates whether or not this object is disabled.
     * <p>
     * The object is disabled when its "disabler" set is not empty
     * <b>or</b> its internal state does not permit the invocation of
     * actions.
     *
     * @return the disabled property.
     */
    ReadOnlyBooleanProperty disabledProperty();

    /**
     * The set of disablers.
     *
     * @return The disablers.
     */
    ObservableSet<Object> disablers();

    // Convenience method
    default boolean isDisabled() {
        return disabledProperty().get();
    }

    /**
     * Adds a disabler.
     *
     * @param disabler a new disabler
     */
    default void addDisabler(Object disabler) {
        disablers().add(disabler);
    }

    /**
     * Removes a disabler.
     *
     * @param disabler an object which does not disable anymore
     */
    default void removeDisabler(Object disabler) {
        disablers().remove(disabler);
    }
}
