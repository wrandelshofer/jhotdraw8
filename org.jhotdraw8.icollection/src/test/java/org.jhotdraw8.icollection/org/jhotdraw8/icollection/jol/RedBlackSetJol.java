package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.RedBlackSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * RedBlackSetJol.
 */
public class RedBlackSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.RedBlackSet with 1000 elements.
     * total size              : 64056
     * element size            : 24
     * data size               : 24000 37%
     * data structure size     : 40056 62%
     * overhead per element    : 40.056 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.RedBlackSet@4ebff610d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.RedBlackSet
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *       1000        40     40000   org.jhotdraw8.icollection.impl.redblack.Node
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       2003               64056   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        RedBlackSet<Key> setA = RedBlackSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.RedBlackSet with 250 elements.
     * total size              : 16056
     * element size            : 24
     * data size               : 6000 37%
     * data structure size     : 10056 62%
     * overhead per element    : 40.224 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.RedBlackSet@95e33ccd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.RedBlackSet
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *        250        40     10000   org.jhotdraw8.icollection.impl.redblack.Node
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        503               16056   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        RedBlackSet<Key> setA = RedBlackSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
