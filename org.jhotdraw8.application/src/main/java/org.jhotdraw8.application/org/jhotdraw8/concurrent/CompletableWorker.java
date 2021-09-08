package org.jhotdraw8.concurrent;

import javafx.concurrent.Worker;

import java.util.concurrent.CompletionStage;

/**
 * A {@code CompletableWorker} is a {@link Worker} that
 * provides value that it produces in a {@link java.util.concurrent.CompletionStage}
 * which completes (or fails) on the FX Application Thread.
 *
 * @param <V> the result type
 */
public interface CompletableWorker<V> extends Worker<V> {
    /**
     * Returns the completion stage of this worker.
     * <p>
     * The completion stage completes (or fails) on the fxApplicationThread
     * when this worker is set to one of the following states:
     * <ul>
     *     <li>{@link Worker.State#FAILED}</li>
     *     <li>{@link Worker.State#SUCCEEDED}</li>
     * </ul>
     *
     * @return
     */
    CompletionStage<V> getCompletionStage();
}
