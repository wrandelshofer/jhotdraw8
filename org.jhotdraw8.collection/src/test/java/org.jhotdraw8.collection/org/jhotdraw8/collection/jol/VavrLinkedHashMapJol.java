package org.jhotdraw8.collection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

/**
 * <pre>
 * class io.vavr.collection.LinkedHashMap with 1000000 elements.
 * total size              : 139872904
 * element size            : 48
 * data size               : 48000000 34%
 * data structure size     : 91872904 65%
 * ----footprint---
 * io.vavr.collection.LinkedHashMap@5a37d3edd footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     343746        33  11622088   [Ljava.lang.Object;
 *    1000000        24  24000000   io.vavr.Tuple2
 *      16897        24    405528   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
 *          1        16        16   io.vavr.collection.HashArrayMappedTrieModule$EmptyNode
 *     326849        24   7844376   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
 *        102        32      3264   io.vavr.collection.HashArrayMappedTrieModule$LeafList
 *     999898        24  23997552   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
 *          1        16        16   io.vavr.collection.HashMap
 *          1        24        24   io.vavr.collection.LinkedHashMap
 *    1000000        24  24000000   io.vavr.collection.List$Cons
 *          1        16        16   io.vavr.collection.List$Nil
 *          1        24        24   io.vavr.collection.Queue
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    5687497           139872904   (total)
 * </pre>
 */
public class VavrLinkedHashMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var mapA = LinkedHashMap.empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Object, Object> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
