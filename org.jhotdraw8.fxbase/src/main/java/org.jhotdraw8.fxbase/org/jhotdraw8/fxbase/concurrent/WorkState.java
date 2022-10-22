/*
 * @(#)WorkState.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.concurrent;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Work state can be used to report the current state
 * of work, and provides an ability to cancel work in progress.
 */
public interface WorkState<V> extends Worker<V> {
    /**
     * The name of the {@link #titleProperty()}.
     */
    String TITLE_PROPERTY = "title";
    /**
     * The name of the {@link #messageProperty()}.
     */
    String MESSAGE_PROPERTY = "message";
    String RUNNING_PROPERTY = "running";
    /**
     * The name of the {@link #valueProperty()}.
     */
    String VALUE_PROPERTY = "value";
    String STATE_PROPERTY = "state";
    String EXCEPTION_PROPERTY = "exception";
    /**
     * The name of the {@link #workDoneProperty()}.
     */
    String WORK_DONE_PROPERTY = "workDone";
    /**
     * The name of the {@link #totalWorkProperty()}.
     */
    String TOTAL_WORK_PROPERTY = "totalWork";
    /**
     * The name of the {@link #progressProperty()}.
     */
    String PROGRESS_PROPERTY = "progress";

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
     * Asynchronously updates the current state of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateState(@NonNull State value);

    /**
     * Asynchronously updates the current exception of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateException(@NonNull Throwable value);

    /**
     * Asynchronously updates the current running state of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateRunning(boolean value);

    /**
     * Asynchronously updates the current title of the work state.
     * <p>
     * Calls to this method are coalesced as described in {@link #updateMessage(String)}.
     *
     * @param value the new value
     */
    void updateTitle(@Nullable String value);

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
    @Override
    @Nullable V getValue();

    /**
     * @see Worker#valueProperty()
     */
    @Override
    @NonNull ReadOnlyObjectProperty<V> valueProperty();


    /**
     * @see Worker#getWorkDone()
     */
    @Override
    double getWorkDone();

    /**
     * @see Worker#workDoneProperty()
     */
    @Override
    @NonNull ReadOnlyDoubleProperty workDoneProperty();


    /**
     * @see Worker#getTotalWork()
     */
    @Override
    double getTotalWork();


    /**
     * @see Worker#totalWorkProperty()
     */
    @Override
    @NonNull ReadOnlyDoubleProperty totalWorkProperty();


    /**
     * @see Worker#getProgress()
     */
    @Override
    double getProgress();

    /**
     * @see Worker#progressProperty()
     */
    @Override
    @NonNull ReadOnlyDoubleProperty progressProperty();

    /**
     * @see Worker#getMessage()
     */
    @Override
    @Nullable String getMessage();

    /**
     * @see Worker#messageProperty()
     */
    @Override
    @NonNull ReadOnlyStringProperty messageProperty();

    /**
     * @see Worker#getTitle()
     */
    @Override
    @Nullable String getTitle();

    /**
     * @see Worker#titleProperty()
     */
    @Override
    @NonNull ReadOnlyStringProperty titleProperty();

}
