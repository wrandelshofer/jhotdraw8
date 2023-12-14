package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.VectorMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * VectorMapJol.
 * <pre>
 * class org.jhotdraw8.icollection.VectorMap with 1000000 elements.
 * total size              : 99007984
 * element size            : 48
 * data size               : 48000000 48%
 * data structure size     : 51007984 51%
 * ----footprint---
 * org.jhotdraw8.icollection.VectorMap@37911f88d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     376323        42  15998672   [Ljava.lang.Object;
 *          1        16        16   org.jhotdraw8.icollection.IdentityObject
 *          1        40        40   org.jhotdraw8.icollection.VectorList
 *          1        40        40   org.jhotdraw8.icollection.VectorMap
 *     343961        32  11006752   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
 *        102        24      2448   org.jhotdraw8.icollection.impl.champ.MutableHashCollisionNode
 *    1000000        24  24000000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
 *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
 *    2000000        24  48000000   org.jhotdraw8.icollection.jmh.Key
 *    3720390            99007984   (total)
 * </pre>
 */
public class VectorMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.VectorMap with 1000 elements.
     * total size              : 99464
     * element size            : 48
     * data size               : 48000 48%
     * data structure size     : 51464 51%
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@5669c5fbd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        383        42     16184   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.IdentityObject
     *          1        40        40   org.jhotdraw8.icollection.VectorList
     *          1        40        40   org.jhotdraw8.icollection.VectorMap
     *        349        32     11168   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3736               99464   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        VectorMap<Key, Key> mapA = VectorMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.VectorMap with 250 elements.
     * total size              : 29528
     * element size            : 48
     * data size               : 12000 40%
     * data structure size     : 17528 59%
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@2d9caaebd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         93        53      4936   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.IdentityObject
     *          1        40        40   org.jhotdraw8.icollection.VectorList
     *          1        40        40   org.jhotdraw8.icollection.VectorMap
     *         75        32      2400   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *        252        24      6048   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *        164        24      3936   org.jhotdraw8.icollection.impl.champ.VectorTombstone
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *        504        24     12096   org.jhotdraw8.icollection.jmh.Key
     *       1092               29528   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        VectorMap<Key, Key> mapA = VectorMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
