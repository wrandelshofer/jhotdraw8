package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.champ.ChampMap;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class org.jhotdraw8.collection.champ.ChampMap with 1000000 elements.
 * total size              : 94362736
 * element size            : 48
 * data size               : 48000000 50%
 * data structure size     : 46362736 49%
 * ----footprint---
 * org.jhotdraw8.collection.champ.ChampMap@723e88f9d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344064        32  11353488   [Ljava.lang.Object;
 *    1000000        24  24000000   java.util.AbstractMap$SimpleImmutableEntry
 *          1        16        16   org.jhotdraw8.collection.IdentityObject
 *          1        32        32   org.jhotdraw8.collection.champ.ChampMap
 *     343961        32  11006752   org.jhotdraw8.collection.champ.MutableBitmapIndexedNode
 *        102        24      2448   org.jhotdraw8.collection.champ.MutableHashCollisionNode
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    3688129            94362736   (total)
 * </pre>
 */
public class ChampMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ChampMap<Key, Key> mapA = ChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
