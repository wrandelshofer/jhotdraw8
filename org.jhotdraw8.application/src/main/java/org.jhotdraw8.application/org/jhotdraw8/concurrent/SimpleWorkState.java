/*
 * @(#)SimpleWorkState.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.concurrent;

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
 */
public class SimpleWorkState<V> implements WorkState<V> {
    private final @NonNull ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", null);
    private final @NonNull AtomicReference<Object> titleUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, "message", null);
    private final @NonNull AtomicReference<Object> messageUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyObjectWrapper<V> value = new ReadOnlyObjectWrapper<>(this, "state", null);
    private final @NonNull AtomicReference<Object> valueUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, "workDone", -1.0);
    private final @NonNull AtomicReference<Object> workDoneUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper(this, "totalWork", -1.0);
    private final @NonNull AtomicReference<Object> totalWorkUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", -1.0);
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
    public void updateValue(@Nullable V newValue) {
        update(newValue, value, valueUpdate);
    }

    public @Nullable V getValue() {
        return value.get();
    }

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

    public double getWorkDone() {
        return workDone.get();
    }

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

    public double getTotalWork() {
        return totalWork.get();
    }


    public @NonNull ReadOnlyDoubleProperty totalWorkProperty() {
        return totalWork.getReadOnlyProperty();
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    public void updateProgress(double newValue) {
        update(newValue, progress, progressUpdate);
    }

    public double getProgress() {
        return progress.get();
    }

    public @NonNull ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public @Nullable String getMessage() {
        return message.get();
    }

    public @NonNull ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    public @Nullable String getTitle() {
        return title.get();
    }

    public @NonNull ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    @Override
    public void updateMessage(@Nullable String value) {
        update(value, message, messageUpdate);
    }

    @Override
    public void updateTitle(String value) {
        update(value, title, titleUpdate);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        isCancelled = true;
    }
}
