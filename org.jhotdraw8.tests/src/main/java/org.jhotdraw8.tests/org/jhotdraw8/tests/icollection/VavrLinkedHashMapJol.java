package org.jhotdraw8.tests.icollection;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

public class VavrLinkedHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class io.vavr.collection.LinkedHashMap with 1000 elements.
    /// total size              : 138984
    /// element size            : 48
    /// data size               : 48000 34%
    /// data structure size     : 90984 65%
    /// overhead per element    : 90.984 bytes
    /// ----footprint---
    /// io.vavr.collection.LinkedHashMap@7403c468d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        324        33     10720   [Ljava.lang.Object;
    ///       1000        24     24000   io.vavr.Tuple2
    ///          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
    ///        323        24      7752   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
    ///         51        32      1632   io.vavr.collection.HashArrayMappedTrieModule$LeafList
    ///        949        24     22776   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
    ///          1        16        16   io.vavr.collection.HashMap
    ///          1        24        24   io.vavr.collection.LinkedHashMap
    ///       1000        24     24000   io.vavr.collection.List$Cons
    ///          1        16        16   io.vavr.collection.List$Nil
    ///          1        24        24   io.vavr.collection.Queue
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       5652              138984   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        var mapA = LinkedHashMap.empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Object, Object> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }

    /// <pre>
    /// class io.vavr.collection.LinkedHashMap with 250 elements.
    /// total size              : 34808
    /// element size            : 48
    /// data size               : 12000 34%
    /// data structure size     : 22808 65%
    /// overhead per element    : 91.232 bytes
    /// ----footprint---
    /// io.vavr.collection.LinkedHashMap@2bd08376d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         83        32      2736   [Ljava.lang.Object;
    ///        250        24      6000   io.vavr.Tuple2
    ///          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
    ///         82        24      1968   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
    ///        250        24      6000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
    ///          1        16        16   io.vavr.collection.HashMap
    ///          1        24        24   io.vavr.collection.LinkedHashMap
    ///        250        24      6000   io.vavr.collection.List$Cons
    ///          1        16        16   io.vavr.collection.List$Nil
    ///          1        24        24   io.vavr.collection.Queue
    ///        500        24     12000   org.jhotdraw8.icollection.jmh.Key
    ///       1420               34808   (total)
    /// </pre>
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
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
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
