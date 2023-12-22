package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * ChampMapJol.
 */
public class ChampMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampMap with 1000 elements.
     * total size              : 94768
     * element size            : 48
     * data size               : 48000 50%
     * data structure size     : 46768 49%
     * ----footprint---
     * org.jhotdraw8.icollection.ChampMap@1e44b638d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        350        32     11528   [Ljava.lang.Object;
     *       1000        24     24000   java.util.AbstractMap$SimpleImmutableEntry
     *          1        24        24   org.jhotdraw8.icollection.ChampMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        350        32     11200   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3702               94768   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
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
