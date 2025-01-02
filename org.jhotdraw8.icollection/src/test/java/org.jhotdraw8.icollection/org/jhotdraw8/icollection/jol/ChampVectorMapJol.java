package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampVectorMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableSequencedMapJol.
 */
public class ChampVectorMapJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampVectorMap with 1000 elements.
     * total size              : 95736
     * element size            : 48
     * data size               : 48000 50%
     * data structure size     : 47736 49%
     * overhead per element    : 47.736 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.ChampVectorMap@512baff6d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        316        46     14568   [Ljava.lang.Object;
     *          1        32        32   org.jhotdraw8.icollection.ChampVectorMap
     *          1        16        16   org.jhotdraw8.icollection.VectorList
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        283        32      9056   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Value
     *       3604               95736   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;
        var data = generateMap(size, mask, size * 10);
        ChampVectorMap<Key, Value> mapA = ChampVectorMap.copyOf(data);
            estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampVectorMap with 250 elements.
     * total size              : 26832
     * element size            : 48
     * data size               : 12000 44%
     * data structure size     : 14832 55%
     * overhead per element    : 59.328 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.ChampVectorMap@32c726eed footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        115        47      5432   [Ljava.lang.Object;
     *          1        32        32   org.jhotdraw8.icollection.ChampVectorMap
     *          1        16        16   org.jhotdraw8.icollection.VectorList
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *         95        32      3040   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *          3        24        72   org.jhotdraw8.icollection.impl.champ.MutableHashCollisionNode
     *        250        24      6000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
     *         11        16       176   org.jhotdraw8.icollection.impl.champ.Tombstone
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Value
     *        979               26832   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        ChampVectorMap<Key, Value> mapA = ChampVectorMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
