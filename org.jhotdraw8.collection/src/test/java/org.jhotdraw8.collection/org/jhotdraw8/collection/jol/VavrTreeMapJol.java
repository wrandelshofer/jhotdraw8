package org.jhotdraw8.collection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.TreeMap;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

/**
 * <pre>
 * class io.vavr.collection.TreeMap with 1000000 elements.
 * total size              : 112000192
 * element size            : 48
 * data size               : 48000000 42%
 * data structure size     : 64000192 57%
 * ----footprint---
 * io.vavr.collection.TreeMap@2fba3fc4d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *          2        24        48   [B
 *    1000000        24  24000000   io.vavr.Tuple2
 *          2        24        48   io.vavr.collection.RedBlackTree$Color
 *          1        16        16   io.vavr.collection.RedBlackTreeModule$Empty
 *    1000000        40  40000000   io.vavr.collection.RedBlackTreeModule$Node
 *          1        16        16   io.vavr.collection.TreeMap
 *          1        16        16   io.vavr.collection.TreeMap$EntryComparator$Natural
 *          2        24        48   java.lang.String
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    4000009           112000192   (total)
 * </pre>
 */
public class VavrTreeMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var mapA = TreeMap.<Key, Key>empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
