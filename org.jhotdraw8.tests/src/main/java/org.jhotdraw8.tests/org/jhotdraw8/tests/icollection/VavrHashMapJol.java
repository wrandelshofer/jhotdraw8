package org.jhotdraw8.tests.icollection;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

public class VavrHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class io.vavr.collection.HashMap with 1000 elements.
    /// total size              : 90920
    /// element size            : 48
    /// data size               : 48000 52%
    /// data structure size     : 42920 47%
    /// overhead per element    : 42.92 bytes
    /// ----footprint---
    /// io.vavr.collection.HashMap@43bd930ad footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        324        33     10720   [Ljava.lang.Object;
    ///          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
    ///        323        24      7752   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
    ///         51        32      1632   io.vavr.collection.HashArrayMappedTrieModule$LeafList
    ///        949        24     22776   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
    ///          1        16        16   io.vavr.collection.HashMap
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3649               90920   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        var mapA = HashMap.empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Object, Object> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }

    /// <pre>
    /// class io.vavr.collection.HashMap with 250 elements.
    /// total size              : 22664
    /// element size            : 48
    /// data size               : 12000 52%
    /// data structure size     : 10664 47%
    /// overhead per element    : 42.656 bytes
    /// ----footprint---
    /// io.vavr.collection.HashMap@77128536d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         81        33      2704   [Ljava.lang.Object;
    ///          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
    ///         80        24      1920   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
    ///        250        24      6000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
    ///          1        16        16   io.vavr.collection.HashMap
    ///        500        24     12000   org.jhotdraw8.icollection.jmh.Key
    ///        913               22664   (total)
    /// </pre>
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
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
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
