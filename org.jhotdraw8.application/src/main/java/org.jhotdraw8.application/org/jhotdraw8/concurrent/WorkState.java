/*
 * @(#)WorkState.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.concurrent;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Work state can be used to report the current state
 * of work, and to provide an ability to cancel work in progress.
 */
public interface WorkState<V> {
    /**
     * Asynchronously updates the current message of the work state.
     * <p>
     * Calls to this method
     * are coalesced and run later on the FX application thread, so calls
     * to this method, even from the FX Application thread, may not
     * necessarily result in immediate updates to the property, and
     * intermediate values may be coalesced to save on event
     * notifications.
     *
     * @param value the new value
     */
    void updateMessage(@Nullable String value);

    /**
     * Asynchronously updates the current title of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param title the new value
     */
    void updateTitle(@Nullable String title);

    /**
     * Asynchronously updates the current value of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateValue(@Nullable V value);

    /**
     * Asynchronously updates the progress of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateProgress(double value);

    /**
     * Returns true if the worker associated to this work state should cancel.
     *
     * @return true if cancelled
     */
    boolean isCancelled();

    /**
     * @see Worker#getValue()
     */
    @Nullable V getValue();

    /**
     * @see Worker#valueProperty()
     */
    @NonNull ReadOnlyObjectProperty<V> valueProperty();


    /**
     * @see Worker#getWorkDone()
     */
    double getWorkDone();

    /**
     * @see Worker#workDoneProperty()
     */
    @NonNull ReadOnlyDoubleProperty workDoneProperty();


    /**
     * @see Worker#getTotalWork()
     */
    double getTotalWork();


    /**
     * @see Worker#totalWorkProperty()
     */
    @NonNull ReadOnlyDoubleProperty totalWorkProperty();


    /**
     * @see Worker#getProgress()
     */
    double getProgress();

    /**
     * @see Worker#progressProperty()
     */
    @NonNull ReadOnlyDoubleProperty progressProperty();

    /**
     * @see Worker#getMessage()
     */
    @Nullable String getMessage();

    /**
     * @see Worker#messageProperty()
     */
    @NonNull ReadOnlyStringProperty messageProperty();

    /**
     * @see Worker#getTitle()
     */
    @Nullable String getTitle();

    /**
     * @see Worker#titleProperty()
     */
    @NonNull ReadOnlyStringProperty titleProperty();


    /**
     * @see Worker#cancel()
     */
    void cancel();
}
