package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampMap2;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * ChampMapJol.
 */
public class ChampMap2Jol extends AbstractJol {

    /**
     * <pre>
     *  # Compressed references (oops): enabled:
     * class org.jhotdraw8.icollection.ChampMap2 with 1000 elements.
     * total size              : 72280
     * element size            : 48
     * data size               : 48000 66%
     * data structure size     : 24280 33%
     * overhead per element    : 24.28 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap2@4b213651d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        310        46     14320   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.ChampMap2
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
        var data = generateMap(size, mask);
        ChampMap2<Key, Key> mapA = ChampMap2.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampMap2 with 1000 elements.
     * total size              : 69632
     * element size            : 48
     * data size               : 48000 68%
     * data structure size     : 21632 31%
     * overhead per element    : 21.632 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap2@6c2ed0cdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        307        46     14240   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.ChampMap2
     *        307        24      7368   org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       2615               69632   (total)
     * </pre>
     * <pre>
     * class org.jhotdraw8.icollection.ChampMap2 with 1 elements.
     * total size              : 120
     * element size            : 48
     * data size               : 48 40%
     * data structure size     : 72 60%
     * overhead per element    : 72.0 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap2@6c2ed0cdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        24        24   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.ChampMap2
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
        var data = generateMap(size, mask);
        ChampMap2<Key, Key> mapA = ChampMap2.of();
        for (var e : data.entrySet()) {
            mapA = mapA.put(e.getKey(), e.getValue());
        }
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * class org.jhotdraw8.icollection.ChampMap2 with 1000 elements.
     * <p>
     * both versions:
     * total size              : 92872
     * element size            : 48
     * <p>
     * mapA:
     * total size              : 92480
     * <p>
     * mapB:
     * total size              : 92464
     * element size            : 48
     * <p>
     * Difference: 92872 - 92480 = 392 bytes
     */
    @Test
    @Disabled
    public void estimateMemoryUsageForANewVersion() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask);
        ChampMap2<Key, Key> mapA = ChampMap2.copyOf(data);
        Key updatedKey = data.keySet().iterator().next();
        ChampMap2<Key, Key> mapB = mapA.put(updatedKey, new Key(mapA.get(updatedKey).value + 1, -1));
        AbstractMap.SimpleImmutableEntry<ChampMap2<Key, Key>, ChampMap2<Key, Key>> twoVersions = new AbstractMap.SimpleImmutableEntry<>(mapA, mapB);
        System.out.println("\nboth versions:");
        estimateMemoryUsage(twoVersions, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapA:");
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapB:");
        estimateMemoryUsage(mapB, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.champ.ChampMap2 with 250 elements.
     * total size              : 23224
     * element size            : 48
     * data size               : 12000 51%
     * data structure size     : 11224 48%
     * ----footprint---
     * org.jhotdraw8.icollection.champ.ChampMap2@651aed93d footprint:
     * COUNT       AVG       SUM   DESCRIPTION
     *  79        33      2664   [Ljava.lang.Object;
     * 250        24      6000   java.util.AbstractMap$SimpleImmutableEntry
     *   2        16        32   org.jhotdraw8.icollection.IdentityObject
     *   1        32        32   org.jhotdraw8.icollection.champ.ChampMap2
     *  78        32      2496   org.jhotdraw8.icollection.champ.MutableBitmapIndexedNode
     * 500        24     12000   org.jhotdraw8.icollection.jmh.Key
     * 910               23224   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ChampMap2<Key, Key> mapA = ChampMap2.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
