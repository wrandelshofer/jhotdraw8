package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.champ.SequencedChampMap;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

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

    /**
     * <pre>
     * class org.jhotdraw8.collection.champ.SequencedChampMap with 1000 elements.
     * total size              : 112440
     * element size            : 48
     * data size               : 48000 42%
     * data structure size     : 64440 57%
     * ----footprint---
     * org.jhotdraw8.collection.champ.SequencedChampMap@4a00d9cfd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        613        33     20800   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.collection.IdentityObject
     *        612        32     19584   org.jhotdraw8.collection.champ.MutableBitmapIndexedNode
     *          1        40        40   org.jhotdraw8.collection.champ.SequencedChampMap
     *       1000        24     24000   org.jhotdraw8.collection.champ.SequencedEntry
     *       2000        24     48000   org.jhotdraw8.collection.jmh.Key
     *       4227              112440   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        SequencedChampMap<Key, Key> mapA = SequencedChampMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.collection.champ.SequencedChampMap with 250 elements.
     * total size              : 27496
     * element size            : 48
     * data size               : 12000 43%
     * data structure size     : 15496 56%
     * ----footprint---
     * org.jhotdraw8.collection.champ.SequencedChampMap@52815fa3d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        163        33      5456   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.collection.IdentityObject
     *        150        24      3600   org.jhotdraw8.collection.champ.BitmapIndexedNode
     *         12        32       384   org.jhotdraw8.collection.champ.MutableBitmapIndexedNode
     *          1        40        40   org.jhotdraw8.collection.champ.SequencedChampMap
     *        250        24      6000   org.jhotdraw8.collection.champ.SequencedEntry
     *        500        24     12000   org.jhotdraw8.collection.jmh.Key
     *       1077               27496   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        SequencedChampMap<Key, Key> mapA = SequencedChampMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = (int) (keys.size() * 0.75); i > 0; i--) {
            mapA = mapA.remove(keys.get(i));
        }


        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }


}
