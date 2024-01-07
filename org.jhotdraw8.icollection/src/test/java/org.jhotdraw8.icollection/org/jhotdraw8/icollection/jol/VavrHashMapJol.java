package org.jhotdraw8.icollection.jol;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

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
 *    2000000        24  48000000   org.jhotdraw8.icollection.jmh.Key
 *    3687494            91872840   (total)
 * </pre>
 */
public class VavrHashMapJol extends AbstractJol {

    /**
     * <pre>
     * class io.vavr.collection.HashMap with 1000 elements.
     * total size              : 91944
     * element size            : 48
     * data size               : 48000 52%
     * data structure size     : 43944 47%
     * overhead per element    : 43.944 bytes
     * ----footprint---
     * io.vavr.collection.HashMap@3bd323e9d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        350        32     11528   [Ljava.lang.Object;
     *          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
     *        349        24      8376   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
     *       1000        24     24000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
     *          1        16        16   io.vavr.collection.HashMap
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       3701               91944   (total)
     * </pre>
     */
    @Test
    @Disabled
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

    /**
     * <pre>
     * class io.vavr.collection.HashMap with 250 elements.
     * total size              : 22664
     * element size            : 48
     * data size               : 12000 52%
     * data structure size     : 10664 47%
     * overhead per element    : 42.656 bytes
     * ----footprint---
     * io.vavr.collection.HashMap@77128536d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         81        33      2704   [Ljava.lang.Object;
     *          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
     *         80        24      1920   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
     *        250        24      6000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
     *          1        16        16   io.vavr.collection.HashMap
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        913               22664   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var mapA = HashMap.empty();
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
