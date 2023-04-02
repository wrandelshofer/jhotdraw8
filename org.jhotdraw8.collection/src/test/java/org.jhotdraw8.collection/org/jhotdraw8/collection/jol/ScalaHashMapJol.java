package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.ReusableBuilder;

import java.util.AbstractMap;

/**
 * <pre>
 * class scala.collection.immutable.HashMap with 1000000 elements.
 * total size              : 83774440
 * element size            : 48
 * data size               : 48000000 57%
 * data structure size     : 35774440 42%
 * ----footprint---
 * scala.collection.immutable.HashMap@6138e79ad footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     301911        30   9139048   [I
 *     308422        46  14293600   [Ljava.lang.Object;
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *        204        24      4896   scala.Tuple2
 *     308320        40  12332800   scala.collection.immutable.BitmapIndexedMapNode
 *        102        24      2448   scala.collection.immutable.HashCollisionMapNode
 *          1        16        16   scala.collection.immutable.HashMap
 *        102        16      1632   scala.collection.immutable.Vector1
 *    2919062            83774440   (total)
 * </pre>
 */
public class ScalaHashMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        ReusableBuilder<Tuple2<Key, Key>, HashMap<Key, Key>> b = HashMap.newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        HashMap<Key, Key> mapA = b.result();
        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
