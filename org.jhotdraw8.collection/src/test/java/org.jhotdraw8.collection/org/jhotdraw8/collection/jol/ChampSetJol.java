package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.champ.ChampSet;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * copyOf
 *
 * class org.jhotdraw8.collection.champ.ChampSet with 1000000 elements.
 *
 * total size              : 46371840
 * element size            : 24
 * data size               : 24000000 51%
 * data structure size     : 22371840 48%
 * </pre>
 * <pre>
 * oneByOne
 *
 * class org.jhotdraw8.collection.champ.ChampSet with 1000000 elements.
 *
 * total size              : 43618704
 * element size            : 24
 * data size               : 24000000 55%
 * data structure size     : 19618704 44%
 *
 * ----footprint---
 * org.jhotdraw8.collection.champ.ChampSet@1130520dd footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344264        32  11356360   [Ljava.lang.Object;
 *     344140        24   8259360   org.jhotdraw8.collection.champ.BitmapIndexedNode
 *          1        32        32   org.jhotdraw8.collection.champ.ChampSet
 *        123        24      2952   org.jhotdraw8.collection.champ.HashCollisionNode
 *    1000000        24  24000000   org.jhotdraw8.collection.jmh.Key
 *    1688528            43618704   (total)
 *
 * Addresses are stable after 1 tries.
 * </pre>
 */
public class ChampSetJol extends AbstractJol {

    @Test
    public void estimateMemoryUsageCopyOf() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        ChampSet<Key> setA = ChampSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    @Test
    public void estimateMemoryUsageOneByOne() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        ChampSet<Key> setA = ChampSet.of();
        for (var d : data) {
            setA = setA.add(d);
        }
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
