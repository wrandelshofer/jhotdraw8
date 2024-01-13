package org.jhotdraw8.icollection.jol;

import io.vavr.collection.TreeSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * VavrTreeSetJol.
 */
public class VavrTreeSetJol extends AbstractJol {

    /**
     * <pre>
     * Class io.vavr.collection.TreeSet with 1000 elements.
     * total size              : 64192
     * element size            : 24
     * data size               : 24000 37%
     * data structure size     : 40192 62%
     * overhead per element    : 40.192 bytes
     * ----footprint---
     * io.vavr.collection.TreeSet@d771cc9d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          2        24        48   [B
     *          1        16        16   io.vavr.collection.NaturalComparator
     *          2        24        48   io.vavr.collection.RedBlackTree$Color
     *          1        16        16   io.vavr.collection.RedBlackTreeModule$Empty
     *       1000        40     40000   io.vavr.collection.RedBlackTreeModule$Node
     *          1        16        16   io.vavr.collection.TreeSet
     *          2        24        48   java.lang.String
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       2009               64192   (total)
     * </pre>
     */
    @Disabled
    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        var setA = TreeSet.<Key>empty();
        for (var d : data) {
            setA = setA.add(d);
        }
        Key head = setA.head();
        estimateMemoryUsage(setA, head, setA.size());
    }


}
