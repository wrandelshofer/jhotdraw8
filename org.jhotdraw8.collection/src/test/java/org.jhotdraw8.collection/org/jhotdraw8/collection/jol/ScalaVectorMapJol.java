package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.VectorMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

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

    /**
     * <pre>
     * class scala.collection.immutable.VectorMap with 1000 elements.
     * total size              : 127576
     * element size            : 48
     * data size               : 48000 37%
     * data structure size     : 79576 62%
     * ----footprint---
     * scala.collection.immutable.VectorMap@545b995ed footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        30      8992   [I
     *        328        56     18528   [Ljava.lang.Object;
     *          1       136       136   [[Ljava.lang.Object;
     *       1000        16     16000   java.lang.Integer
     *       2000        24     48000   org.jhotdraw8.collection.jmh.Key
     *       1000        24     24000   scala.Tuple2
     *        296        40     11840   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.VectorMap
     *       4921              127576   (total)
     * </pre>
     */
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

    /**
     * <pre>
     * class scala.collection.immutable.VectorMap with 250 elements.
     * total size              : 45792
     * element size            : 48
     * data size               : 12000 26%
     * data structure size     : 33792 73%
     * ----footprint---
     * scala.collection.immutable.VectorMap@524f3b3ad footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         52        36      1896   [I
     *         84        90      7600   [Ljava.lang.Object;
     *          1       136       136   [[Ljava.lang.Object;
     *        250        16      4000   java.lang.Integer
     *        500        24     12000   org.jhotdraw8.collection.jmh.Key
     *        250        24      6000   scala.Tuple2
     *         52        40      2080   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.VectorMap
     *        750        16     12000   scala.collection.immutable.VectorMap$Tombstone
     *       1942               45792   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var b = VectorMap.<Key, Key>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        VectorMap<Key, Key> mapA = b.result();

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = (int) (keys.size() * 0.75); i > 0; i--) {
            mapA = mapA.removed(keys.get(i));
        }


        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
