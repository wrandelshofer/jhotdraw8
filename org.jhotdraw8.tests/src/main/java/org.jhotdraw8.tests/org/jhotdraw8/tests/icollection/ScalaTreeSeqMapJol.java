package org.jhotdraw8.tests.icollection;

import scala.Tuple2;
import scala.collection.immutable.TreeSeqMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;


public class ScalaTreeSeqMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class scala.collection.immutable.TreeSeqMap with 1000 elements.
    /// total size              : 180520
    /// element size            : 48
    /// data size               : 48000 26%
    /// data structure size     : 132520 73%
    /// overhead per element    : 132.52 bytes
    /// ----footprint---
    /// scala.collection.immutable.TreeSeqMap@6fe7aac8d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        315        29      9336   [I
    ///        317        45     14472   [Ljava.lang.Object;
    ///       1000        16     16000   java.lang.Integer
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       1000        24     24000   scala.Tuple2
    ///        317        40     12680   scala.collection.immutable.BitmapIndexedMapNode
    ///          1        16        16   scala.collection.immutable.HashMap
    ///          1        32        32   scala.collection.immutable.TreeSeqMap
    ///          1        16        16   scala.collection.immutable.TreeSeqMap$OrderBy$Insertion$
    ///        999        32     31968   scala.collection.immutable.TreeSeqMap$Ordering$Bin
    ///       1000        24     24000   scala.collection.immutable.TreeSeqMap$Ordering$Tip
    ///       6951              180520   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        var b = TreeSeqMap.<Key, Value>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        TreeSeqMap<Key, Value> mapA = b.result();
        Tuple2<Key, Value> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());

    }

    /// ```
    /// class scala.collection.immutable.TreeSeqMap with 250 elements.
    /// total size              : 43488
    /// element size            : 48
    /// data size               : 12000 27%
    /// data structure size     : 31488 72%
    /// ----footprint---
    /// scala.collection.immutable.TreeSeqMap@1e097d59d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         57        34      1976   [I
    ///         57        56      3200   [Ljava.lang.Object;
    ///        250        16      4000   java.lang.Integer
    ///        500        24     12000   org.jhotdraw8.icollection.jmh.Key
    ///        250        24      6000   scala.Tuple2
    ///         57        40      2280   scala.collection.immutable.BitmapIndexedMapNode
    ///          1        16        16   scala.collection.immutable.HashMap
    ///          1        32        32   scala.collection.immutable.TreeSeqMap
    ///          1        16        16   scala.collection.immutable.TreeSeqMap$OrderBy$Insertion$
    ///        249        32      7968   scala.collection.immutable.TreeSeqMap$Ordering$Bin
    ///        250        24      6000   scala.collection.immutable.TreeSeqMap$Ordering$Tip
    ///       1673               43488   (total)
    /// ```
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        var b = TreeSeqMap.<Key, Value>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        TreeSeqMap<Key, Value> mapA = b.result();

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = 0, n = (int) (keys.size() * 0.75); i < n; i++) {
            mapA = mapA.removed(keys.get(i));
        }

        Tuple2<Key, Value> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
