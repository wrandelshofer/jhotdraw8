package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FontCollectionsFactory {
    List<FontCollection> create();

    /**
     * Creates a FontChooserModel asynchronously in a worker thread, and completes the returned
     * {@link CompletableFuture} on the JavaFX Application Thread.
     *
     * @return a {@link CompletableFuture}.
     */
    default CompletableFuture<List<FontCollection>> createAsync() {
        CompletableFuture<List<FontCollection>> future = new CompletableFuture<>();
        Task<List<FontCollection>> task = new Task<>() {
            @Override
            protected List<FontCollection> call() {
                return create();
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> future.completeExceptionally(getException()));
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> future.complete(getValue()));
            }
        };
        new Thread(task).start();
        return future;
    }
}
