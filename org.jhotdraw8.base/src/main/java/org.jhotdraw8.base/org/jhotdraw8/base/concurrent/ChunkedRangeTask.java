/*
 * @(#)RangeTask.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.jhotdraw8.base.concurrent;

import java.util.concurrent.CountedCompleter;
import java.util.function.BiConsumer;

/**
 * A fork-join task that processes a range of integers from {@code lo} to {@code hi} (exclusive)
 * in chunks.
 */
public class ChunkedRangeTask extends CountedCompleter<Void> {
    final int lo, hi;
    final int chunkSize;
    final BiConsumer<Integer, Integer> chunkConsumer;

    ChunkedRangeTask(ChunkedRangeTask parent, int lo, int hi, int chunkSize, BiConsumer<Integer, Integer> chunkConsumer) {
        super(parent, ((hi - lo - 1) / chunkSize));
        this.chunkSize = chunkSize;
        this.lo = lo;
        this.hi = hi;
        this.chunkConsumer = chunkConsumer;
    }

    public void compute() {
        int n = lo;
        for (; n < hi - chunkSize; n += chunkSize)
            new ChunkedRangeTask(this, n, n + chunkSize, chunkSize, chunkConsumer).fork();
        chunkConsumer.accept(n, Math.min(n + chunkSize, hi));
        propagateCompletion();
    }

    public static void forEach(int lo, int hi, int threshold, BiConsumer<Integer, Integer> action) {
        if (hi - lo > 0) {
            new ChunkedRangeTask(null, lo, hi, threshold, action).invoke();
        }
    }
}