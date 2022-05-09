/*
 * @(#)SimpleWorkState.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.concurrent;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import static org.jhotdraw8.concurrent.FXConcurrentUtil.NO_UPDATE_IS_IN_PROGRESS;
import static org.jhotdraw8.concurrent.FXConcurrentUtil.update;

/**
 * A simple implementation of the {@link WorkState} interface.
 * <p>
 * This implementation requires that the FX Application Thread is running.
 */
public class SimpleWorkState<V> implements WorkState<V> {
    private final @NonNull ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, TITLE_PROPERTY, null);
    private final @NonNull AtomicReference<Object> titleUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, MESSAGE_PROPERTY, null);
    private final @NonNull ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, RUNNING_PROPERTY, true);
    private final @NonNull AtomicReference<Object> messageUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyObjectWrapper<V> value = new ReadOnlyObjectWrapper<>(this, VALUE_PROPERTY, null);
    private final @NonNull ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(this, STATE_PROPERTY, State.READY);
    private final @NonNull AtomicReference<Object> stateUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull AtomicReference<Object> runningUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull AtomicReference<Object> exceptionUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyObjectWrapper<Throwable> exception = new ReadOnlyObjectWrapper<>(this, EXCEPTION_PROPERTY, null);
    private final @NonNull AtomicReference<Object> valueUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, WORK_DONE_PROPERTY, -1.0);
    private final @NonNull AtomicReference<Object> workDoneUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper(this, TOTAL_WORK_PROPERTY, -1.0);
    private final @NonNull AtomicReference<Object> totalWorkUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, PROGRESS_PROPERTY, -1.0);
    private final @NonNull AtomicReference<Object> progressUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private volatile boolean isCancelled;

    /**
     * Creates a new instance.
     */
    public SimpleWorkState() {
    }

    /**
     * Creates a new instance with the specified title.
     *
     * @param title a title
     */
    public SimpleWorkState(@Nullable String title) {
        updateTitle(title);
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    @Override
    public void updateValue(@Nullable V newValue) {
        update(newValue, value, valueUpdate);
    }

    @Override
    public @Nullable V getValue() {
        return value.get();
    }

    @Override
    public @NonNull ReadOnlyObjectProperty<V> valueProperty() {
        return value;
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    public void updateWorkDone(double newValue) {
        update(newValue, workDone, workDoneUpdate);
    }

    @Override
    public double getWorkDone() {
        return workDone.get();
    }

    @Override
    public @NonNull ReadOnlyDoubleProperty workDoneProperty() {
        return workDone.getReadOnlyProperty();
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    public void updateTotalWork(double newValue) {
        update(newValue, totalWork, totalWorkUpdate);
    }

    @Override
    public double getTotalWork() {
        return totalWork.get();
    }


    @Override
    public @NonNull ReadOnlyDoubleProperty totalWorkProperty() {
        return totalWork.getReadOnlyProperty();
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    @Override
    public void updateProgress(double newValue) {
        update(newValue, progress, progressUpdate);
    }

    @Override
    public double getProgress() {
        return progress.get();
    }

    @Override
    public @NonNull ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getMessage() {
        return message.get();
    }

    @Override
    public @NonNull ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getTitle() {
        return title.get();
    }

    @Override
    public @NonNull ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    @Override
    public void updateMessage(@Nullable String value) {
        update(value, message, messageUpdate);
    }

    @Override
    public void updateState(@Nullable State value) {
        update(value, state, stateUpdate);
    }

    @Override
    public void updateException(@Nullable Throwable value) {
        update(value, exception, exceptionUpdate);
    }

    @Override
    public void updateRunning(boolean value) {
        update(value, running, runningUpdate);
    }

    @Override
    public void updateTitle(String value) {
        update(value, title, titleUpdate);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean cancel() {
        isCancelled = true;
        return true;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state;
    }

    @Override
    public Throwable getException() {
        return exception.get();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return exception;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public ReadOnlyBooleanProperty runningProperty() {
        return running;
    }
}
