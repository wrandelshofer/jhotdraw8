/*
 * @(#)CompletableWorker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.concurrent.Worker;

import java.util.concurrent.CompletionStage;

/// A `CompletableWorker` is a [Worker] that
/// provides value that it produces in a [CompletionStage]
/// which completes (or fails) on the FX Application Thread.
///
/// @param <V> the result type
public interface CompletableWorker<V> extends Worker<V> {
    /// Returns the completion stage of this worker.
    ///
    /// The completion stage completes (or fails) on the fxApplicationThread
    /// when this worker is set to one of the following states:
    ///
    ///   - [Worker.State#FAILED]
    ///   - [Worker.State#SUCCEEDED]
    ///
    /// @return
    CompletionStage<V> getCompletionStage();

    void complete(V result);

    void completeExceptionally(Throwable exception);
}
