package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.HashSet;

/**
 * ScalaHashSetJol.
 */
public class ScalaHashSetJol extends AbstractJol {

    /**
     * <pre>
     * class scala.collection.immutable.HashSet with 1000 elements.
     * total size              : 56592
     * element size            : 24
     * data size               : 24000 42%
     * data structure size     : 32592 57%
     * overhead per element    : 32.592 bytes
     * ----footprint---
     * scala.collection.immutable.HashSet@4982cc36d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        311        29      9288   [I
     *        316        33     10648   [Ljava.lang.Object;
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *        316        40     12640   scala.collection.immutable.BitmapIndexedSetNode
     *          1        16        16   scala.collection.immutable.HashSet
     *       1944               56592   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        var b = HashSet.<Key>newBuilder();
        for (var d : data) {
            b.addOne(d);
        }
        HashSet<Key> setA = b.result();
        Key head = setA.head();
        estimateMemoryUsage(setA, head, setA.size());
    }


}
