package org.jhotdraw8.icollection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

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
 *    2000000        24  48000000   org.jhotdraw8.icollection.jmh.Key
 *    5687497           139872904   (total)
 * </pre>
 */
public class VavrLinkedHashMapJol extends AbstractJol {

    /**
     * <pre>
     * class io.vavr.collection.LinkedHashMap with 1000 elements.
     * total size              : 140008
     * element size            : 48
     * data size               : 48000 34%
     * data structure size     : 92008 65%
     * ----footprint---
     * io.vavr.collection.LinkedHashMap@4f704591d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        350        32     11528   [Ljava.lang.Object;
     *       1000        24     24000   io.vavr.Tuple2
     *          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
     *        349        24      8376   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
     *       1000        24     24000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
     *          1        16        16   io.vavr.collection.HashMap
     *          1        24        24   io.vavr.collection.LinkedHashMap
     *       1000        24     24000   io.vavr.collection.List$Cons
     *          1        16        16   io.vavr.collection.List$Nil
     *          1        24        24   io.vavr.collection.Queue
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       5704              140008   (total)
     * </pre>
     */
    @Test
    @Disabled
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

    /**
     * <pre>
     * class io.vavr.collection.LinkedHashMap with 250 elements.
     * total size              : 34696
     * element size            : 48
     * data size               : 12000 34%
     * data structure size     : 22696 65%
     * ----footprint---
     * io.vavr.collection.LinkedHashMap@78a773fdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         81        32      2672   [Ljava.lang.Object;
     *        250        24      6000   io.vavr.Tuple2
     *          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
     *         80        24      1920   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
     *        250        24      6000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
     *          1        16        16   io.vavr.collection.HashMap
     *          1        24        24   io.vavr.collection.LinkedHashMap
     *        250        24      6000   io.vavr.collection.List$Cons
     *          1        16        16   io.vavr.collection.List$Nil
     *          1        24        24   io.vavr.collection.Queue
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *       1416               34696   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var mapA = LinkedHashMap.empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = (int) (keys.size() * 0.75); i > 0; i--) {
            mapA = mapA.remove(keys.get(i));
        }


        Tuple2<Object, Object> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
