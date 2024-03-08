package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FontCollectionsFactory {
    @NonNull List<FontCollection> create();

    /**
     * Creates a FontChooserModel asynchronously in a worker thread, and completes the returned
     * {@link CompletableFuture} on the JavaFX Application Thread.
     *
     * @return a {@link CompletableFuture}.
     */
    default @NonNull CompletableFuture<List<FontCollection>> createAsync() {
        CompletableFuture<List<FontCollection>> future = new CompletableFuture<>();
        Task<List<FontCollection>> task = new Task<>() {
            @Override
            protected List<FontCollection> call() throws Exception {
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
