package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;


public class JavaUnmodifiableMapJol extends AbstractJol {
    /**
     * <pre>
     * class java.util.ImmutableCollections$MapN with 1000 elements.
     * total size              : 64048
     * element size            : 48
     * data size               : 48000 74%
     * data structure size     : 16048 25%
     * overhead per element    : 16.048 bytes
     * ----footprint---
     * java.util.ImmutableCollections$MapN@37e4d7bbd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1     16016     16016   [Ljava.lang.Object;
     *          1        32        32   java.util.ImmutableCollections$MapN
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Value
     *       2002               64048   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);

        Map<Key, Value> mapA = Map.copyOf(data);
        estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }
}
