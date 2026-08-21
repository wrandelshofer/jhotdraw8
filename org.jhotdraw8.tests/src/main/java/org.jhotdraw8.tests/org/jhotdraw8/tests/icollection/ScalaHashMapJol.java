package org.jhotdraw8.tests.icollection;


import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.ReusableBuilder;

import java.util.AbstractMap;

public class ScalaHashMapJol extends AbstractJol {

    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class scala.collection.immutable.HashMap with 1000 elements.
    /// total size              : 84504
    /// element size            : 48
    /// data size               : 48000 56%
    /// data structure size     : 36504 43%
    /// overhead per element    : 36.504 bytes
    /// ----footprint---
    /// scala.collection.immutable.HashMap@7a4ccb53d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        315        29      9336   [I
    ///        317        45     14472   [Ljava.lang.Object;
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Key
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Value
    ///        317        40     12680   scala.collection.immutable.BitmapIndexedMapNode
    ///          1        16        16   scala.collection.immutable.HashMap
    ///       2950               84504   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        ReusableBuilder<Tuple2<Object, Object>, HashMap<Object, Object>> b = HashMap.newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        HashMap<Object, Object> mapA = b.result();
        Tuple2<Object, Object> head = mapA.head();
        AbstractJol.estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
