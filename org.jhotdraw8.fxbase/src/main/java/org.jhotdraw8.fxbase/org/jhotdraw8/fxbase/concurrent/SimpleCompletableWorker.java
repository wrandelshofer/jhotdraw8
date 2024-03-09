/*
 * @(#)SimpleCompletableWorker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import org.jhotdraw8.annotation.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.jhotdraw8.fxbase.concurrent.PlatformUtil.update;

/**
 * Simple implementation of {@code CompletableWorker}.
 *
 * @param <V> the result type
 */
public class SimpleCompletableWorker<V> implements CompletableWorker<V> {
    private static final @NonNull Object NO_UPDATE_IS_IN_PROGRESS = new Object();
    private final WorkState<V> workState;
    private final CompletableFuture<V> completableFuture = new CompletableFuture<>();
    private final @NonNull ReadOnlyObjectWrapper<Worker.State> state = new ReadOnlyObjectWrapper<>(this, "state", Worker.State.READY);
    private final @NonNull AtomicReference<Object> stateUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyObjectWrapper<Throwable> exception = new ReadOnlyObjectWrapper<>(this, "exception", null);
    private final @NonNull AtomicReference<Object> exceptionUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);
    private final @NonNull ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, "running", true);
    private final @NonNull AtomicReference<Object> runningUpdate = new AtomicReference<>(NO_UPDATE_IS_IN_PROGRESS);

    public SimpleCompletableWorker(WorkState<V> workState) {
        this.workState = workState;
    }

    @Override
    public CompletionStage<V> getCompletionStage() {
        return completableFuture;
    }

    @Override
    public Worker.@NonNull State getState() {
        return state.get();
    }

    @Override
    public @NonNull ReadOnlyObjectProperty<Worker.State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /**
     * This method may be called on any thread.
     *
     * @param newValue the new value
     */
    public void updateState(Worker.@NonNull State newValue) {
        update(newValue, state, stateUpdate);
    }


    @Override
    public V getValue() {
        return workState.getValue();
    }

    @Override
    public ReadOnlyObjectProperty<V> valueProperty() {
        return workState.valueProperty();
    }

    @Override
    public Throwable getException() {
        return exception.get();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return exception.getReadOnlyProperty();
    }

    @Override
    public double getWorkDone() {
        return workState.getWorkDone();
    }

    @Override
    public ReadOnlyDoubleProperty workDoneProperty() {
        return workState.workDoneProperty();
    }

    @Override
    public double getTotalWork() {
        return workState.getTotalWork();
    }

    @Override
    public ReadOnlyDoubleProperty totalWorkProperty() {
        return workState.totalWorkProperty();
    }

    @Override
    public double getProgress() {
        return workState.getProgress();
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return workState.progressProperty();
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
    public String getMessage() {
        return workState.getMessage();
    }

    @Override
    public ReadOnlyStringProperty messageProperty() {
        return workState.messageProperty();
    }

    @Override
    public String getTitle() {
        return workState.getTitle();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return workState.titleProperty();
    }

    @Override
    public boolean cancel() {
        workState.cancel();
        return completableFuture.cancel(true);
    }

    public void updateException(Throwable value) {
        update(value, exception, exceptionUpdate);
    }

    @Override
    public void complete(V result) {
        workState.updateValue(result);
        updateState(Worker.State.SUCCEEDED);
        update(false, running, runningUpdate);
        completableFuture.complete(result);
    }

    @Override
    public void completeExceptionally(Throwable exception) {
        updateException(exception);
        updateState(State.FAILED);
        update(false, running, runningUpdate);
        completableFuture.completeExceptionally(exception);
    }
}
