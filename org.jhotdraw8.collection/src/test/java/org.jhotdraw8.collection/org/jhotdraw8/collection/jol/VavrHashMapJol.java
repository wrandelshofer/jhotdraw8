package org.jhotdraw8.collection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

/**
 * <pre>
 * class io.vavr.collection.HashMap with 1000000 elements.
 * total size              : 91872840
 * element size            : 48
 * data size               : 48000000 52%
 * data structure size     : 43872840 47%
 * ----footprint---
 * io.vavr.collection.HashMap@a82c5f1d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     343746        33  11622088   [Ljava.lang.Object;
 *      16897        24    405528   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
 *          1        16        16   io.vavr.collection.HashArrayMappedTrieModule$EmptyNode
 *     326849        24   7844376   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
 *        102        32      3264   io.vavr.collection.HashArrayMappedTrieModule$LeafList
 *     999898        24  23997552   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
 *          1        16        16   io.vavr.collection.HashMap
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    3687494            91872840   (total)
 * </pre>
 */
public class VavrHashMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var mapA = HashMap.empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Object, Object> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
