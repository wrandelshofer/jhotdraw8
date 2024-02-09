package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.RedBlackMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableNavigableMapJol.
 */
public class RedBlackMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableNavigableMap with 1000 elements.
     * total size              : 88056
     * element size            : 48
     * data size               : 48000 54%
     * data structure size     : 40056 45%
     * overhead per element    : 40.056 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableNavigableMap@6c2ed0cdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableMap
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *       1000        40     40000   org.jhotdraw8.icollection.impl.redblack.Node
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3003               88056   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        RedBlackMap<Key, Value> mapA = RedBlackMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableNavigableMap with 250 elements.
     * total size              : 22056
     * element size            : 48
     * data size               : 12000 54%
     * data structure size     : 10056 45%
     * overhead per element    : 40.224 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableNavigableMap@4b213651d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableMap
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *        250        40     10000   org.jhotdraw8.icollection.impl.redblack.Node
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        753               22056   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        RedBlackMap<Key, Value> mapA = RedBlackMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
