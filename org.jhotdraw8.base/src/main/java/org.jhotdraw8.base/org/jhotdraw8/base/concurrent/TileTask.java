/*
 * @(#)TileTask.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.concurrent;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

/**
 * A fork-join task that processes a range of integers from {@code lo} to {@code hi} (exclusive)
 * in chunks of up to {@code chunkSize}.
 */
public class TileTask extends CountedCompleter<Void> {
    private final @NonNull Tile tile;
    private final int chunkSize;
    private final @NonNull Consumer<Tile> tileConsumer;
    private final @NonNull CompletableFuture<Void> future;

    public TileTask(@NonNull Tile tile, int chunkSize, @NonNull Consumer<Tile> tileConsumer, @NonNull CompletableFuture<Void> future) {
        this(null, tile, chunkSize, tileConsumer, future);
    }

    TileTask(@Nullable TileTask parent, @NonNull Tile tile, int chunkSize, @NonNull Consumer<Tile> tileConsumer, @NonNull CompletableFuture<Void> future) {
        super(parent, ((tile.xto - tile.xfrom + chunkSize - 1) / chunkSize) * ((tile.yto - tile.yfrom + chunkSize - 1) / chunkSize) - 1);
        this.chunkSize = chunkSize;
        this.tile = tile;
        this.tileConsumer = tileConsumer;
        this.future = future;
    }

    public static void forEach(int x, int y, int width, int height, int chunkSize, Consumer<Tile> action) {
        new TileTask(null, new Tile(x, y, x + width, y + height), chunkSize, action, new CompletableFuture<>()).invoke();
    }

    public static CompletableFuture<Void> fork(int x, int y, int width, int height, int chunkSize, Consumer<Tile> action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TileTask rangeTask = new TileTask(null, new Tile(x, y, x + width, y + height), chunkSize, action, future);
        rangeTask.fork();
        return future;
    }

    @Override
    public void compute() {
        if (getRoot() == this) {
            // Fork all except the first tile at the top left
            for (int y = tile.yfrom; y < tile.yto; y += chunkSize) {
                for (int x = tile.xfrom; x < tile.xto; x += chunkSize) {
                    if (y != 0 || x != 0) {
                        new TileTask(this, new Tile(x, y, Math.min(x + chunkSize, tile.xto),
                                Math.min(y + chunkSize, tile.yto)), chunkSize, tileConsumer, future).fork();
                    }
                }
            }
        }
        if (!future.isCancelled()) {
            // Perform the first tile at the top left
            tileConsumer.accept(new Tile(tile.xfrom, tile.yfrom,
                    Math.min(tile.xto, tile.xfrom + chunkSize),
                    Math.min(tile.yto, tile.yto + chunkSize)));
        }
        tryComplete();
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (getRoot().getPendingCount() == 0) {
            future.complete(null);
        }
    }

    @Override
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        future.completeExceptionally(ex);
        return true;
    }

    public record Tile(int xfrom, int yfrom, int xto, int yto) {
    }
}
