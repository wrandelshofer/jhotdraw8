package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.TreeSeqMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

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
 *    2000000        24  48000000   org.jhotdraw8.icollection.jmh.Key
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

    /**
     * <pre>
     * class scala.collection.immutable.TreeSeqMap with 1000 elements.
     * total size              : 178880
     * element size            : 48
     * data size               : 48000 26%
     * data structure size     : 130880 73%
     * ----footprint---
     * scala.collection.immutable.TreeSeqMap@1c33c17bd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        30      8992   [I
     *        296        47     14016   [Ljava.lang.Object;
     *       1000        16     16000   java.lang.Integer
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   scala.Tuple2
     *        296        40     11840   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.TreeSeqMap
     *          1        16        16   scala.collection.immutable.TreeSeqMap$OrderBy$Insertion$
     *        999        32     31968   scala.collection.immutable.TreeSeqMap$Ordering$Bin
     *       1000        24     24000   scala.collection.immutable.TreeSeqMap$Ordering$Tip
     *       6887              178880   (total)
     * </pre>
     */
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

    /**
     * <pre>
     * class scala.collection.immutable.TreeSeqMap with 250 elements.
     * total size              : 43488
     * element size            : 48
     * data size               : 12000 27%
     * data structure size     : 31488 72%
     * ----footprint---
     * scala.collection.immutable.TreeSeqMap@1e097d59d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         57        34      1976   [I
     *         57        56      3200   [Ljava.lang.Object;
     *        250        16      4000   java.lang.Integer
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        250        24      6000   scala.Tuple2
     *         57        40      2280   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.TreeSeqMap
     *          1        16        16   scala.collection.immutable.TreeSeqMap$OrderBy$Insertion$
     *        249        32      7968   scala.collection.immutable.TreeSeqMap$Ordering$Bin
     *        250        24      6000   scala.collection.immutable.TreeSeqMap$Ordering$Tip
     *       1673               43488   (total)
     * </pre>
     */
    @Test
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);
        var b = TreeSeqMap.<Key, Key>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        TreeSeqMap<Key, Key> mapA = b.result();

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = 0, n = (int) (keys.size() * 0.75); i < n; i++) {
            mapA = mapA.removed(keys.get(i));
        }

        Tuple2<Key, Key> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
