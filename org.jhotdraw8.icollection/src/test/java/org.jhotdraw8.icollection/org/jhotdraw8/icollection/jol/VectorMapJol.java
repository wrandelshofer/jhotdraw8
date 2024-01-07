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
     * total size              : 97216
     * element size            : 48
     * data size               : 48000 49%
     * data structure size     : 49216 50%
     * overhead per element    : 49.216 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@42530531d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        343        44     15176   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.VectorList
     *          1        32        32   org.jhotdraw8.icollection.VectorMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        310        32      9920   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3658               97216   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsage() {
        int size = 1_000;
            final int mask = -1;//~64;
            var data = generateMap(size, mask);
            VectorMap<Key, Key> mapA = VectorMap.copyOf(data);
            estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.VectorMap with 250 elements.
     * total size              : 26288
     * element size            : 48
     * data size               : 12000 45%
     * data structure size     : 14288 54%
     * overhead per element    : 57.152 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.VectorMap@b4711e2d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        102        50      5128   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.VectorList
     *          1        32        32   org.jhotdraw8.icollection.VectorMap
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *         85        32      2720   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *        250        24      6000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *         20        16       320   org.jhotdraw8.icollection.impl.champ.Tombstone
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        962               26288   (total)
     * </pre>
     */
    @Test
    @Disabled

    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        VectorMap<Key, Key> mapA = VectorMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
