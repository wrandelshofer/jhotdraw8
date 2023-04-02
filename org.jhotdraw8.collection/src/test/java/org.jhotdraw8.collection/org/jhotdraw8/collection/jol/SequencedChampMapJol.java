package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.champ.SequencedChampMap;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class org.jhotdraw8.collection.champ.SequencedChampMap with 1000000 elements.
 * total size              : 111782344
 * element size            : 48
 * data size               : 48000000 42%
 * data structure size     : 63782344 57%
 * ----footprint---
 * org.jhotdraw8.collection.champ.SequencedChampMap@3e44f2a5d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     602133        34  20514880   [Ljava.lang.Object;
 *          1        16        16   org.jhotdraw8.collection.IdentityObject
 *     602030        32  19264960   org.jhotdraw8.collection.champ.MutableBitmapIndexedNode
 *        102        24      2448   org.jhotdraw8.collection.champ.MutableHashCollisionNode
 *          1        40        40   org.jhotdraw8.collection.champ.SequencedChampMap
 *    1000000        24  24000000   org.jhotdraw8.collection.champ.SequencedEntry
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    4204267           111782344   (total)
 * </pre>
 */
public class SequencedChampMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        SequencedChampMap<Key, Key> mapA = SequencedChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }


}
