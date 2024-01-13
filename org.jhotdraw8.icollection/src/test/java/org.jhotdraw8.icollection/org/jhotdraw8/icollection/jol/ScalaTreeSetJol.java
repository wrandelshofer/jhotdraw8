package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.TreeSet;
import scala.math.Ordering;

/**
 * ScalaTreeSetJol.
 */
public class ScalaTreeSetJol extends AbstractJol {

    /**
     * <pre>
     * class scala.collection.immutable.TreeSet with 1000 elements.
     * total size              : 56072
     * element size            : 24
     * data size               : 24000 42%
     * data structure size     : 32072 57%
     * overhead per element    : 32.072 bytes
     * ----footprint---
     * scala.collection.immutable.TreeSet@7e4204e2d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *          1        16        16   org.jhotdraw8.icollection.jol.ScalaTreeSetJol
     *          1        16        16   org.jhotdraw8.icollection.jol.ScalaTreeSetJol$1
     *       1000        32     32000   scala.collection.immutable.RedBlackTree$Tree
     *          1        24        24   scala.collection.immutable.TreeSet
     *          1        16        16   scala.runtime.BoxedUnit
     *       2004               56072   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        var b = TreeSet.<Key>newBuilder(new Ordering<Key>() {
            @Override
            public int compare(Key x, Key y) {
                return y.value - x.value;
            }

        });
        for (var d : data) {
            b.addOne(d);
        }
        TreeSet<Key> setA = b.result();
        Key head = setA.head();
        estimateMemoryUsage(setA, head, setA.size());
    }


}
