package org.jhotdraw8.icollection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.TreeMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

/**
 * VavrTreeMapJol.
 */
public class VavrTreeMapJol extends AbstractJol {

    /**
     * class io.vavr.collection.TreeMap with 1000 elements.
     * total size              : 112192
     * element size            : 48
     * data size               : 48000 42%
     * data structure size     : 64192 57%
     * overhead per element    : 64.192 bytes
     * ----footprint---
     * io.vavr.collection.TreeMap@be68757d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          2        24        48   [B
     *       1000        24     24000   io.vavr.Tuple2
     *          2        24        48   io.vavr.collection.RedBlackTree$Color
     *          1        16        16   io.vavr.collection.RedBlackTreeModule$Empty
     *       1000        40     40000   io.vavr.collection.RedBlackTreeModule$Node
     *          1        16        16   io.vavr.collection.TreeMap
     *          1        16        16   io.vavr.collection.TreeMap$EntryComparator$Natural
     *          2        24        48   java.lang.String
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       4009              112192   (total)
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        var mapA = TreeMap.<Key, Value>empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Key, Value> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
