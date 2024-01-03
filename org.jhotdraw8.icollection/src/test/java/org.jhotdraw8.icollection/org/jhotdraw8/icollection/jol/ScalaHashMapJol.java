package org.jhotdraw8.icollection.jol;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.ReusableBuilder;

import java.util.AbstractMap;

/**
 * ScalaHashMapJol.
 */
public class ScalaHashMapJol extends AbstractJol {

    /**
     * <pre>
     * class scala.collection.immutable.HashMap with 1000 elements.
     * total size              : 82864
     * element size            : 48
     * data size               : 48000 57%
     * data structure size     : 34864 42%
     * ----footprint---
     * scala.collection.immutable.HashMap@4ff8d125d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        30      8992   [I
     *        296        47     14016   [Ljava.lang.Object;
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *        296        40     11840   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *       2886               82864   (total)
     * </pre>
     * With 1 Mio elements, memory overhead is 35.728536 bytes per entry.
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask);
        ReusableBuilder<Tuple2<Object, Object>, HashMap<Object, Object>> b = HashMap.newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        HashMap<Object, Object> mapA = b.result();
        Tuple2<Object, Object> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
