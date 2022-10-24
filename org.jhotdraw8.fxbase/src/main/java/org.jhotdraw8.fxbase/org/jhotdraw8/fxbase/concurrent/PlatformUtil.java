/*
 * @(#)FXConcurrentUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

import javafx.application.Platform;
import javafx.beans.property.Property;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformUtil {
    /**
     * This object is used to coalesce multiple updates.
     */
    final static @NonNull Object NO_UPDATE_IS_IN_PROGRESS = new Object();

    /**
     * Don't let anyone instantiate this class.
     **/
    private PlatformUtil() {

    }

    /**
     * Updates the provided property with the provided new value on the fxApplicationThread.
     * <p>
     * This method coalesce updates. So it is safe to call it very often from a worker thread.
     *
     * @param newValue       the new value
     * @param property       the property
     * @param propertyUpdate this atomic reference is used by this method to coalesce multiple update calls,
     *                       it must have been created with the initial value {@link #NO_UPDATE_IS_IN_PROGRESS}
     * @param <X>            the property type
     */
    @SuppressWarnings("unchecked")
    static <X> void update(@Nullable X newValue, @NonNull Property<X> property, @NonNull AtomicReference<Object> propertyUpdate) {
        if (Platform.isFxApplicationThread()) {
            property.setValue(newValue);
        } else if (propertyUpdate.getAndSet(newValue) == NO_UPDATE_IS_IN_PROGRESS) {
            Platform.runLater(() -> {
                X andSet = (X) propertyUpdate.getAndSet(NO_UPDATE_IS_IN_PROGRESS);
                property.setValue(andSet);
            });
        }
    }

    /**
     * Executes a runnable on the JavaFX Application thread and waits 1
     * minute until it completes.
     *
     * @param r the runnable
     */
    public static void invokeAndWait(@NonNull RunnableWithException r) {
        invokeAndWait(60 * 1000, r);
    }

    /**
     * Executes a runnable on the JavaFX Application thread and waits until it
     * completes.
     *
     * @param timeout the timeout in milliseconds
     * @param r       the runnable
     */
    public static void invokeAndWait(long timeout, @NonNull RunnableWithException r) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                r.run();
                f.complete(null);
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });

        try {
            f.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException |
                 TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a callable on the JavaFX Application thread and waits 1
     * minute until it completes.
     *
     * @param r the runnable
     */
    public static <T> T callAndWait(@NonNull Callable<T> r) {
        return callAndWait(60 * 1000, r);
    }

    /**
     * Executes a callable on the JavaFX Application thread and waits until it
     * completes.
     *
     * @param timeout the timeout in milliseconds
     * @param r       the runnable
     */
    public static <T> T callAndWait(long timeout, @NonNull Callable<T> r) {
        CompletableFuture<T> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                f.complete(r.call());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });

        try {
            return f.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException |
                 TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
