package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.SimpleImmutableMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableMapJol.
 */
public class SimpleImmutableMapJol extends AbstractJol {

    /**
     * <pre>
     *  # Compressed references (oops): enabled:
     * class org.jhotdraw8.icollection.SimpleImmutableMap with 1000 elements.
     * total size              : 72280
     * element size            : 48
     * data size               : 48000 66%
     * data structure size     : 24280 33%
     * overhead per element    : 24.28 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableMap@4b213651d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        310        46     14320   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        310        32      9920   org.jhotdraw8.icollection.impl.champmap.MutableBitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       2622               72280   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        SimpleImmutableMap<Key, Value> mapA = SimpleImmutableMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableMap with 1000 elements.
     * total size              : 68528
     * element size            : 48
     * data size               : 48000 70%
     * data structure size     : 20528 29%
     * overhead per element    : 20.528 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableMap@95e33ccd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        283        48     13712   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableMap
     *        283        24      6792   org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Value
     *       2567               68528   (total)
     * </pre>
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableMap with 1 elements.
     * total size              : 120
     * element size            : 48
     * data size               : 48 40%
     * data structure size     : 72 60%
     * overhead per element    : 72.0 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableMap@6c2ed0cdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        24        24   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableMap
     *          1        24        24   org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode
     *          2        24        48   org.jhotdraw8.icollection.jmh.Key
     *          5                 120   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageNoBulkOperations() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        SimpleImmutableMap<Key, Value> mapA = SimpleImmutableMap.of();
        for (var e : data.entrySet()) {
            mapA = mapA.put(e.getKey(), e.getValue());
        }
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * class org.jhotdraw8.icollection.SimpleImmutableMap with 1000 elements.
     * <p>
     * both versions:
     * total size              : 71256
     * <p>
     * mapA:
     * total size              : 70808
     * <p>
     * mapB:
     * total size              : 70792
     * <p>
     * Difference: 71256 - 70808 = 448 bytes
     */
    @Test
    @Disabled
    public void estimateMemoryUsageForANewVersion() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10L);
        SimpleImmutableMap<Key, Value> mapA = SimpleImmutableMap.copyOf(data);
        Key updatedKey = data.keySet().iterator().next();
        SimpleImmutableMap<Key, Value> mapB = mapA.put(updatedKey, new Value(mapA.get(updatedKey).value + 1, -1));
        AbstractMap.SimpleImmutableEntry<SimpleImmutableMap<Key, Value>, SimpleImmutableMap<Key, Value>> twoVersions = new AbstractMap.SimpleImmutableEntry<>(mapA, mapB);
        System.out.println("\nboth versions:");
        estimateMemoryUsage(twoVersions, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapA:");
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapB:");
        estimateMemoryUsage(mapB, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.champ.SimpleImmutableMap with 250 elements.
     * total size              : 23224
     * element size            : 48
     * data size               : 12000 51%
     * data structure size     : 11224 48%
     * ----footprint---
     * org.jhotdraw8.icollection.champ.SimpleImmutableMap@651aed93d footprint:
     * COUNT       AVG       SUM   DESCRIPTION
     *  79        33      2664   [Ljava.lang.Object;
     * 250        24      6000   java.util.AbstractMap$SimpleImmutableEntry
     *   2        16        32   org.jhotdraw8.icollection.IdentityObject
     *   1        32        32   org.jhotdraw8.icollection.champ.SimpleImmutableMap
     *  78        32      2496   org.jhotdraw8.icollection.champ.MutableBitmapIndexedNode
     * 500        24     12000   org.jhotdraw8.icollection.jmh.Key
     * 910               23224   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        SimpleImmutableMap<Key, Value> mapA = SimpleImmutableMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
