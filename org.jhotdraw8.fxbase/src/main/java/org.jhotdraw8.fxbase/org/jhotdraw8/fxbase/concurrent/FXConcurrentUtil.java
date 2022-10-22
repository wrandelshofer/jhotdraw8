/*
 * @(#)FXConcurrentUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.application.Platform;
import javafx.beans.property.Property;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

class FXConcurrentUtil {
    /**
     * This object is used to coalesce multiple updates.
     */
    final static @NonNull Object NO_UPDATE_IS_IN_PROGRESS = new Object();

    /**
     * Don't let anyone instantiate this class.
     **/
    private FXConcurrentUtil() {

    }

    /**
     * Updates the provided property with the provided new value on the fxApplicationThread.
     * <p>
     * This method coalesce updates. So it is safe to call it very often from a worker thread.
     *
     * @param newValue       the new value
     * @param property       the property
     * @param propertyUpdate this atomic reference is used by this method to coalesce multiple update calls,
     *                       it must have been created with the initial value {@link #NO_UPDATE_IS_IN_PROGRESS}
     * @param <X>            the property type
     */
    @SuppressWarnings("unchecked")
    static <X> void update(@Nullable X newValue, @NonNull Property<X> property, @NonNull AtomicReference<Object> propertyUpdate) {
        if (Platform.isFxApplicationThread()) {
            property.setValue(newValue);
        } else if (propertyUpdate.getAndSet(newValue) == NO_UPDATE_IS_IN_PROGRESS) {
            Platform.runLater(() -> {
                X andSet = (X) propertyUpdate.getAndSet(NO_UPDATE_IS_IN_PROGRESS);
                property.setValue(andSet);
            });
        }
    }
}
