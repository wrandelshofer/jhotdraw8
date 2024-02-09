package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.collection.immutable.VectorMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * ScalaVectorMapJol.
 */
public class ScalaRedBlackMapJol extends AbstractJol {

    /**
     * <pre>
     * class scala.collection.immutable.SimpleImmutableSequencedMap with 1000 elements.
     * total size              : 127560
     * element size            : 48
     * data size               : 48000 37%
     * data structure size     : 79560 62%
     * overhead per element    : 79.56 bytes
     * ----footprint---
     * scala.collection.immutable.SimpleImmutableSequencedMap@46268f08d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        292        30      8968   [I
     *        328        56     18536   [Ljava.lang.Object;
     *          1       136       136   [[Ljava.lang.Object;
     *       1000        16     16000   java.lang.Integer
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   scala.Tuple2
     *        296        40     11840   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.SimpleImmutableSequencedMap
     *       4920              127560   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        var b = VectorMap.<Key, Value>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        VectorMap<Key, Value> mapA = b.result();
        Tuple2<Key, Value> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }

    /**
     * <pre>
     * class scala.collection.immutable.SimpleImmutableSequencedMap with 250 elements.
     * total size              : 46560
     * element size            : 48
     * data size               : 12000 25%
     * data structure size     : 34560 74%
     * overhead per element    : 138.24 bytes
     * ----footprint---
     * scala.collection.immutable.SimpleImmutableSequencedMap@2ceb80a1d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         62        33      2056   [I
     *         94        83      7808   [Ljava.lang.Object;
     *          1       136       136   [[Ljava.lang.Object;
     *        250        16      4000   java.lang.Integer
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        250        24      6000   scala.Tuple2
     *         62        40      2480   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.SimpleImmutableSequencedMap
     *        750        16     12000   scala.collection.immutable.SimpleImmutableSequencedMap$Tombstone
     *       1972               46560   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        var b = VectorMap.<Key, Value>newBuilder();
        for (var d : data.entrySet()) {
            b.addOne(new Tuple2<>(d.getKey(), d.getValue()));
        }
        VectorMap<Key, Value> mapA = b.result();

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        for (int i = (int) (keys.size() * 0.75); i > 0; i--) {
            mapA = mapA.removed(keys.get(i));
        }


        Tuple2<Key, Value> head = mapA.head();
        estimateMemoryUsage(mapA, new AbstractMap.SimpleImmutableEntry<>(head._1, head._2), mapA.size());
    }


}
