/*
 * @(#)RangeTask.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.concurrent;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.function.BiConsumer;

/**
 * A fork-join task that processes a range of integers from {@code lo} to {@code hi} (exclusive)
 * in chunks of up to {@code chunkSize}.
 */
public class RangeTask extends CountedCompleter<Void> {
    private final int lo, hi;
    private final int chunkSize;
    private final @NonNull BiConsumer<Integer, Integer> rangeConsumer;
    private final @NonNull CompletableFuture<Void> future;

    public RangeTask(int lo, int hi, int chunkSize, @NonNull BiConsumer<Integer, Integer> rangeConsumer, @NonNull CompletableFuture<Void> future) {
        this(null, lo, hi, chunkSize, rangeConsumer, future);
    }

    RangeTask(@Nullable RangeTask parent, int lo, int hi, int chunkSize, @NonNull BiConsumer<Integer, Integer> rangeConsumer, @NonNull CompletableFuture<Void> future) {
        super(parent, ((hi - lo - 1) / chunkSize));
        this.chunkSize = chunkSize;
        this.lo = lo;
        this.hi = hi;
        this.rangeConsumer = rangeConsumer;
        this.future = future;
    }

    @Override
    public void compute() {
        int n = lo;
        for (; n < hi - chunkSize; n += chunkSize) {
            new RangeTask(this, n, n + chunkSize, chunkSize, rangeConsumer, future).fork();
        }
        if (!future.isCancelled()) {
            rangeConsumer.accept(n, Math.min(n + chunkSize, hi));
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

    public static void forEach(int lo, int hi, int chunkSize, BiConsumer<Integer, Integer> action) {
        new RangeTask(null, lo, hi, chunkSize, action, new CompletableFuture<>()).invoke();
    }

    public static CompletableFuture<Void> fork(int lo, int hi, int chunkSize, BiConsumer<Integer, Integer> action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        RangeTask rangeTask = new RangeTask(null, lo, hi, chunkSize, action, future);
        rangeTask.fork();
        return future;
    }
}
