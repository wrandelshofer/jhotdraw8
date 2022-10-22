/*
 * @(#)BlackHoleWorkState.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * This work state does not update its properties, except for {@link #isCancelled()}.
 * This allows to use this work state, when the FX Application Thread is not running.
 *
 * @param <V> the result type of the work
 */
public class BlackHoleWorkState<V> implements WorkState<V> {
    private final @NonNull ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, TITLE_PROPERTY, null);
    private final @NonNull ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, MESSAGE_PROPERTY, null);
    private final @NonNull ReadOnlyObjectWrapper<V> value = new ReadOnlyObjectWrapper<>(this, VALUE_PROPERTY, null);
    private final @NonNull ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, WORK_DONE_PROPERTY, -1.0);
    private final @NonNull ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper(this, TOTAL_WORK_PROPERTY, -1.0);
    private final @NonNull ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, PROGRESS_PROPERTY, -1.0);
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
    public void updateState(@NonNull State value) {

    }

    @Override
    public void updateException(@NonNull Throwable value) {

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
        return null;
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return null;
    }

    @Nullable
    @Override
    public V getValue() {
        return value.get();
    }

    @Override
    public @NonNull ReadOnlyObjectProperty<V> valueProperty() {
        return value.getReadOnlyProperty();
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return null;
    }

    @Override
    public double getWorkDone() {
        return workDone.get();
    }

    @Override
    public @NonNull ReadOnlyDoubleProperty workDoneProperty() {
        return workDone.getReadOnlyProperty();
    }

    @Override
    public double getTotalWork() {
        return totalWork.get();
    }

    @Override
    public @NonNull ReadOnlyDoubleProperty totalWorkProperty() {
        return totalWork.getReadOnlyProperty();
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
    public boolean isRunning() {
        return false;
    }

    @Override
    public ReadOnlyBooleanProperty runningProperty() {
        return null;
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
    public boolean cancel() {
        isCancelled = true;
        return true;
    }
}
