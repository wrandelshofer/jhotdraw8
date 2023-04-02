package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.TreeSeqMap;

import java.util.AbstractMap;

/**
 * <pre>
 * class scala.collection.immutable.TreeSeqMap with 1000000 elements.
 * total size              : 179774456
 * element size            : 48
 * data size               : 48000000 26%
 * data structure size     : 131774456 73%
 * ----footprint---
 * scala.collection.immutable.TreeSeqMap@176b3f44d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     301911        30   9139048   [I
 *     308422        46  14293600   [Ljava.lang.Object;
 *    1000000        16  16000000   java.lang.Integer
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    1000204        24  24004896   scala.Tuple2
 *     308320        40  12332800   scala.collection.immutable.BitmapIndexedMapNode
 *        102        24      2448   scala.collection.immutable.HashCollisionMapNode
 *          1        16        16   scala.collection.immutable.HashMap
 *          1        32        32   scala.collection.immutable.TreeSeqMap
 *          1        16        16   scala.collection.immutable.TreeSeqMap$OrderBy$Insertion$
 *     999999        32  31999968   scala.collection.immutable.TreeSeqMap$Ordering$Bin
 *    1000000        24  24000000   scala.collection.immutable.TreeSeqMap$Ordering$Tip
 *        102        16      1632   scala.collection.immutable.Vector1
 *    6919063           179774456   (total)
 * </pre>
 */
public class ScalaTreeSeqMapJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var b = TreeSeqMap.<Key, Key>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        TreeSeqMap<Key, Key> mapA = b.result();
        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
