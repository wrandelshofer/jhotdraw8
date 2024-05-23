/*
 * @(#)FXWorker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import org.jhotdraw8.base.concurrent.CheckedFunction;
import org.jhotdraw8.base.concurrent.CheckedRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * {@code FXWorker} provides convenience methods for
 * executing {@link CheckedRunnable} objects on a
 *
 * @author Werner Randelshofer
 */
public class FXWorker {

    /**
     * Don't let anyone instantiate this class.
     */
    private FXWorker() {
    }

    /**
     * Calls the runnable on a new Thread. The completion stage is
     * completed on the FX Application Thread.
     *
     * @param runnable the runnable
     * @return the CompletableFuture
     */
    public static CompletableFuture<Void> run(CheckedRunnable runnable) {
        return run(ForkJoinPool.commonPool(), runnable);
    }

    /**
     * Calls the runnable on the executor thread. The completion stage is
     * completed on the FX Application Thread.
     *
     * @param runnable the runnable
     * @param executor the executor, if null then a new thread is created
     * @return the CompletableFuture
     */
    public static CompletableFuture<Void> run(Executor executor, CheckedRunnable runnable) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        Runnable worker = () -> {
            try {
                runnable.run();
                Platform.runLater(() -> f.complete(null));
            } catch (Exception e) {
                Platform.runLater(() -> f.completeExceptionally(e));
            }
        };
        executor.execute(worker);
        return f;
    }

    /**
     * Calls the supplier on a thread of the common fork join pool. The completion stage is
     * completed on the FX Application Thread.
     *
     * @param <T>      the value type
     * @param supplier the supplier
     * @return the CompletableFuture
     */
    public static <T> CompletableFuture<T> supply(Callable<T> supplier) {
        return supply(ForkJoinPool.commonPool(), supplier);
    }

    /**
     * Calls the supplier on the executor thread. The  {@link CompletableFuture} is
     * completed on the FX Application Thread.
     *
     * @param <T>      the value type
     * @param supplier the supplier
     * @param executor the executor
     * @return the {@link CompletableFuture}
     */
    public static <T> CompletableFuture<T> supply(Executor executor, Callable<T> supplier) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                T result = supplier.call();
                Platform.runLater(() -> f.complete(result));
            } catch (Throwable e) {
                Platform.runLater(() -> f.completeExceptionally(e));
            }
        });
        return f;
    }

    /**
     * Calls the supplier on the executor thread. The {@link CompletableWorker} is
     * completed on the FX Application Thread.
     *
     * @param <T>      the value type
     * @param supplier the supplier
     * @param executor the executor
     * @return the {@link CompletableWorker}
     */
    public static <T> CompletableWorker<T> work(Executor executor, CheckedFunction<WorkState<T>, T> supplier, WorkState<T> workState) {
        SimpleCompletableWorker<T> w = new SimpleCompletableWorker<>(workState);

        w.updateState(Worker.State.SCHEDULED);
        executor.execute(() -> {
            try {
                w.updateState(Worker.State.RUNNING);
                T result = supplier.apply(workState);
                Platform.runLater(() -> w.complete(result));
            } catch (Throwable e) {
                Platform.runLater(() -> w.completeExceptionally(e));
            }
        });
        return w;
    }
}
