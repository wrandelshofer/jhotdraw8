package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
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
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       1000        24     24000   scala.Tuple2
     *        296        40     11840   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.VectorMap
     *       4921              127576   (total)
     * </pre>
     * With 1 Mio elements, memory overhead is 80.373792 bytes per entry.
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000_000;
        final int mask = -1;//~64;
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
     * lass scala.collection.immutable.VectorMap with 250 elements.
     * total size              : 45944
     * element size            : 48
     * data size               : 12000 26%
     * data structure size     : 33944 73%
     * ----footprint---
     * scala.collection.immutable.VectorMap@eda25e5d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         54        35      1936   [I
     *         86        88      7632   [Ljava.lang.Object;
     *          1       136       136   [[Ljava.lang.Object;
     *        250        16      4000   java.lang.Integer
     *        500        24     12000   org.jhotdraw8.icollection.jmh.Key
     *        250        24      6000   scala.Tuple2
     *         54        40      2160   scala.collection.immutable.BitmapIndexedMapNode
     *          1        16        16   scala.collection.immutable.HashMap
     *          1        32        32   scala.collection.immutable.Vector2
     *          1        32        32   scala.collection.immutable.VectorMap
     *        750        16     12000   scala.collection.immutable.VectorMap$Tombstone
     *       1948               45944   (total)
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
