package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * <pre>
 * class java.util.ImmutableCollections$MapN with 1000000 elements.
 * total size              : 64000048
 * element size            : 48
 * data size               : 48000000 74%
 * data structure size     : 16000048 25%
 * ----footprint---
 * java.util.ImmutableCollections$MapN@51a9ad5ed footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *          1  16000016  16000016   [Ljava.lang.Object;
 *          1        32        32   java.util.ImmutableCollections$MapN
 *    2000000        24  48000000   org.jhotdraw8.icollection.jmh.Key
 *    2000002            64000048   (total)
 * </pre>
 */
public class JavaUnmodifiableMapJol extends AbstractJol {

    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);

        Map<Key, Key> mapA = Map.copyOf(data);
        estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }
}
