package org.jhotdraw8.tests.icollection;

import io.vavr.Tuple2;
import io.vavr.collection.TreeMap;


import java.util.AbstractMap;


public class VavrTreeMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class io.vavr.collection.TreeMap with 1000 elements.
    /// total size              : 112192
    /// element size            : 48
    /// data size               : 48000 42%
    /// data structure size     : 64192 57%
    /// overhead per element    : 64.192 bytes
    /// ----footprint---
    /// io.vavr.collection.TreeMap@8646db9d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          2        24        48   [B
    ///       1000        24     24000   io.vavr.Tuple2
    ///          2        24        48   io.vavr.collection.RedBlackTree$Color
    ///          1        16        16   io.vavr.collection.RedBlackTreeModule$Empty
    ///       1000        40     40000   io.vavr.collection.RedBlackTreeModule$Node
    ///          1        16        16   io.vavr.collection.TreeMap
    ///          1        16        16   io.vavr.collection.TreeMap$EntryComparator$Natural
    ///          2        24        48   java.lang.String
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       4009              112192   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        var mapA = TreeMap.<Key, Value>empty();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        Tuple2<Key, Value> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
