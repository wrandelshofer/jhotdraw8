package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.VectorMap;

import java.util.AbstractMap;

/**
 * <pre>
 * class scala.collection.immutable.VectorMap with 1000000 elements.
 * total size              : 128419696
 * element size            : 48
 * data size               : 48000000 37%
 * data structure size     : 80419696 62%
 * ----footprint---
 * scala.collection.immutable.VectorMap@53dbe163d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     301911        30   9139048   [I
 *     339672        55  18793600   [Ljava.lang.Object;
 *        977       143    140632   [[Ljava.lang.Object;
 *         31       141      4400   [[[Ljava.lang.Object;
 *          1       136       136   [[[[Ljava.lang.Object;
 *    1000000        16  16000000   java.lang.Integer
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    1000204        24  24004896   scala.Tuple2
 *     308320        40  12332800   scala.collection.immutable.BitmapIndexedMapNode
 *        102        24      2448   scala.collection.immutable.HashCollisionMapNode
 *          1        16        16   scala.collection.immutable.HashMap
 *        102        16      1632   scala.collection.immutable.Vector1
 *          1        56        56   scala.collection.immutable.Vector4
 *          1        32        32   scala.collection.immutable.VectorMap
 *    4951323           128419696   (total)
 * </pre>
 */
public class ScalaVectorMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var b = VectorMap.<Key, Key>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        VectorMap<Key, Key> mapA = b.result();
        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
