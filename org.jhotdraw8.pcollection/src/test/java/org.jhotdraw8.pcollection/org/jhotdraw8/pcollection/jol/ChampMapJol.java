package org.jhotdraw8.pcollection.jol;

import org.jhotdraw8.pcollection.ChampMap;
import org.jhotdraw8.pcollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <pre>
 * class org.jhotdraw8.pcollection.champ.ChampMap with 1000000 elements.
 * total size              : 94362736
 * element size            : 48
 * data size               : 48000000 50%
 * data structure size     : 46362736 49%
 * ----footprint---
 * org.jhotdraw8.pcollection.champ.ChampMap@723e88f9d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344064        32  11353488   [Ljava.lang.Object;
 *    1000000        24  24000000   java.util.AbstractMap$SimpleImmutableEntry
 *          1        16        16   org.jhotdraw8.pcollection.IdentityObject
 *          1        32        32   org.jhotdraw8.pcollection.champ.ChampMap
 *     343961        32  11006752   org.jhotdraw8.pcollection.champ.MutableBitmapIndexedNode
 *        102        24      2448   org.jhotdraw8.pcollection.champ.MutableHashCollisionNode
 *    2000000        24  48000000   org.jhotdraw8.pcollection.jmh.Key
 *    3688129            94362736   (total)
 * </pre>
 */
public class ChampMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.pcollection.champ.ChampMap with 1000 elements.
     * total size              : 94744
     * element size            : 48
     * data size               : 48000 50%
     * data structure size     : 46744 49%
     * ----footprint---
     * org.jhotdraw8.pcollection.champ.ChampMap@7a3793c7d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        350        32     11528   [Ljava.lang.Object;
     *       1000        24     24000   java.util.AbstractMap$SimpleImmutableEntry
     *          1        16        16   org.jhotdraw8.pcollection.IdentityObject
     *          1        32        32   org.jhotdraw8.pcollection.champ.ChampMap
     *        349        32     11168   org.jhotdraw8.pcollection.champ.MutableBitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.pcollection.jmh.Key
     *       3701               94744   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.pcollection.champ.ChampMap with 250 elements.
     * total size              : 23224
     * element size            : 48
     * data size               : 12000 51%
     * data structure size     : 11224 48%
     * ----footprint---
     * org.jhotdraw8.pcollection.champ.ChampMap@651aed93d footprint:
     * COUNT       AVG       SUM   DESCRIPTION
     *  79        33      2664   [Ljava.lang.Object;
     * 250        24      6000   java.util.AbstractMap$SimpleImmutableEntry
     *   2        16        32   org.jhotdraw8.pcollection.IdentityObject
     *   1        32        32   org.jhotdraw8.pcollection.champ.ChampMap
     *  78        32      2496   org.jhotdraw8.pcollection.champ.MutableBitmapIndexedNode
     * 500        24     12000   org.jhotdraw8.pcollection.jmh.Key
     * 910               23224   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}