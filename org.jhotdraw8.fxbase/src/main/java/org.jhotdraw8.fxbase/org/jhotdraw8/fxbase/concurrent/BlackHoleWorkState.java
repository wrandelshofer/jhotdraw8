/*
 * @(#)BlackHoleWorkState.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.jspecify.annotations.Nullable;

/**
 * This work state does not update its properties, except for {@link #isCancelled()}.
 * This allows to use this work state, when the FX Application Thread is not running.
 *
 * @param <V> the result type of the work
 */
public class BlackHoleWorkState<V> implements WorkState<V> {
    @SuppressWarnings("this-escape")
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, TITLE_PROPERTY, null);
    @SuppressWarnings("this-escape")
    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, MESSAGE_PROPERTY, null);
    @SuppressWarnings("this-escape")
    private final ReadOnlyObjectWrapper<V> value = new ReadOnlyObjectWrapper<>(this, VALUE_PROPERTY, null);
    @SuppressWarnings("this-escape")
    private final ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, WORK_DONE_PROPERTY, -1.0);
    @SuppressWarnings("this-escape")
    private final ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper(this, TOTAL_WORK_PROPERTY, -1.0);
    @SuppressWarnings("this-escape")
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, PROGRESS_PROPERTY, -1.0);
    @SuppressWarnings("this-escape")
    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, RUNNING_PROPERTY, true);
    private volatile boolean isCancelled;

    public BlackHoleWorkState() {
    }

    /**
     * Does nothing, because this implementation does not require that the
     * FX Application Thread is running.
     *
     * @param value the new value
     */
    @Override
    public void updateMessage(@Nullable String value) {

    }

    @Override
    public void updateState(State value) {

    }

    @Override
    public void updateException(Throwable value) {

    }

    @Override
    public void updateRunning(boolean value) {

    }

    /**
     * Does nothing, because this implementation does not require that the
     * FX Application Thread is running.
     *
     * @param value the new value
     */
    @Override
    public void updateTitle(@Nullable String value) {

    }

    /**
     * Does nothing, because this implementation does not require that the
     * FX Application Thread is running.
     *
     * @param value the new value
     */
    @Override
    public void updateValue(@Nullable Object value) {

    }

    /**
     * Does nothing, because this implementation does not require that the
     * FX Application Thread is running.
     *
     * @param value the new value
     */
    @Override
    public void updateProgress(double value) {

    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public State getState() {
        return state.get();
    }

    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.READY);
    private final ReadOnlyObjectWrapper<Throwable> throwable = new ReadOnlyObjectWrapper<>(null);

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @Nullable
    @Override
    public V getValue() {
        return value.get();
    }

    @Override
    public ReadOnlyObjectProperty<V> valueProperty() {
        return value.getReadOnlyProperty();
    }

    @Override
    public @Nullable Throwable getException() {
        return throwable.get();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return throwable.getReadOnlyProperty();
    }

    @Override
    public double getWorkDone() {
        return workDone.get();
    }

    @Override
    public ReadOnlyDoubleProperty workDoneProperty() {
        return workDone.getReadOnlyProperty();
    }

    @Override
    public double getTotalWork() {
        return totalWork.get();
    }

    @Override
    public ReadOnlyDoubleProperty totalWorkProperty() {
        return totalWork.getReadOnlyProperty();
    }

    @Override
    public double getProgress() {
        return progress.get();
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getMessage() {
        return message.get();
    }

    @Override
    public ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getTitle() {
        return title.get();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    @Override
    public boolean cancel() {
        isCancelled = true;
        return true;
    }
}
