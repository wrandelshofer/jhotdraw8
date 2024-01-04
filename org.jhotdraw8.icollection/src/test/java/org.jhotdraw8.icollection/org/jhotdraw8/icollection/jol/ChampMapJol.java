package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * ChampMapJol.
 */
public class ChampMapJol extends AbstractJol {

    /**
     * <pre>
     *  # Compressed references (oops): enabled:
     * class org.jhotdraw8.icollection.ChampMap with 1000 elements.
     * total size              : 92480
     * element size            : 48
     * data size               : 48000 51%
     * data structure size     : 44480 48%
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap@babafc2d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        310        33     10520   [Ljava.lang.Object;
     *       1000        24     24000   java.util.AbstractMap$SimpleImmutableEntry
     *          1        24        24   org.jhotdraw8.icollection.ChampMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        310        32      9920   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3622               92480   (total)
     * </pre>
     * With 1 Mio elements, memory overhead is 44.307680 bytes per entry.
     * <pre>
     * # Compressed references (oops): disabled:
     * class org.jhotdraw8.icollection.ChampMap with 1000 elements.
     * total size              : 107872
     * element size            : 48
     * data size               : 48000 44%
     * data structure size     : 59872 55%
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap@3f57bcadd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        310        49     15432   [Ljava.lang.Object;
     *       1000        32     32000   java.util.AbstractMap$SimpleImmutableEntry
     *          1        24        24   org.jhotdraw8.icollection.ChampMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        310        40     12400   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3622              107872   (total)
     * </pre>
     *  With 1 Mio elements, memory overhead is 59.676512 bytes per entry.
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampMap with 1000 elements.
     * total size              : 89984
     * element size            : 48
     * data size               : 48000 53%
     * data structure size     : 41984 46%
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap@95e33ccd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        310        33     10520   [Ljava.lang.Object;
     *       1000        24     24000   java.util.AbstractMap$SimpleImmutableEntry
     *          1        24        24   org.jhotdraw8.icollection.ChampMap
     *        310        24      7440   org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3621               89984   (total)
     * </pre>
     * With 1 Mio elements, memory overhead is 41.848104 bytes per entry.
     */
    @Test
    @Disabled
    public void estimateMemoryUsageNoBulkOperations() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.of();
        for (var e : data.entrySet()) {
            mapA = mapA.put(e.getKey(), e.getValue());
        }
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * class org.jhotdraw8.icollection.ChampMap with 1000 elements.
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
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);
        Key updatedKey = data.keySet().iterator().next();
        ChampMap<Key, Key> mapB = mapA.put(updatedKey, new Key(mapA.get(updatedKey).value + 1, -1));
        AbstractMap.SimpleImmutableEntry<ChampMap<Key, Key>, ChampMap<Key, Key>> twoVersions = new AbstractMap.SimpleImmutableEntry<>(mapA, mapB);
        System.out.println("\nboth versions:");
        estimateMemoryUsage(twoVersions, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapA:");
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapB:");
        estimateMemoryUsage(mapB, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.champ.ChampMap with 250 elements.
     * total size              : 23224
     * element size            : 48
     * data size               : 12000 51%
     * data structure size     : 11224 48%
     * ----footprint---
     * org.jhotdraw8.icollection.champ.ChampMap@651aed93d footprint:
     * COUNT       AVG       SUM   DESCRIPTION
     *  79        33      2664   [Ljava.lang.Object;
     * 250        24      6000   java.util.AbstractMap$SimpleImmutableEntry
     *   2        16        32   org.jhotdraw8.icollection.IdentityObject
     *   1        32        32   org.jhotdraw8.icollection.champ.ChampMap
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
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
