package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.VectorMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * VectorMapJol.
 */
public class VectorMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.VectorMap with 1000 elements.
     * total size              : 99504
     * element size            : 48
     * data size               : 48000 48%
     * data structure size     : 51504 51%
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@6bc407fdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        383        42     16184   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.VectorList
     *          1        32        32   org.jhotdraw8.icollection.VectorMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        350        32     11200   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3738               99504   (total)
     * </pre>
     * With 1 Mio elements, memory overhead is 48.952944 bytes per element.
     */
    @Test
    @Disabled

    public void estimateMemoryUsage() {
        for (int i = 1; i <= 1_000_000; i *= 10) {
            int size = i;
            final int mask = -1;//~64;
            var data = generateMap(size, mask);
            VectorMap<Key, Key> mapA = VectorMap.copyOf(data);
            estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
        }
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.VectorMap with 250 elements.
     * total size              : 25824
     * element size            : 48
     * data size               : 12000 46%
     * data structure size     : 13824 53%
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@1583741ed footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         90        54      4888   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.VectorList
     *          1        32        32   org.jhotdraw8.icollection.VectorMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *         73        32      2336   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *        250        24      6000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *         30        16       480   org.jhotdraw8.icollection.impl.champ.Tombstone
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        948               25824   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1;
        final int mask = ~64;
        var data = generateMap(size, mask);
        VectorMap<Key, Key> mapA = VectorMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
