package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 * TreeSetJol.
 */
public class JavaTreeSetJol extends AbstractJol {

    /**
     * <pre>
     * class java.util.TreeSet with 1000 elements.
     * total size              : 64096
     * element size            : 24
     * data size               : 24000 37%
     * data structure size     : 40096 62%
     * overhead per element    : 40.096 bytes
     * ----footprint---
     * java.util.TreeSet@37e4d7bbd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   java.lang.Object
     *          1        48        48   java.util.TreeMap
     *       1000        40     40000   java.util.TreeMap$Entry
     *          1        16        16   java.util.TreeMap$KeySet
     *          1        16        16   java.util.TreeSet
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       2004               64096   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        TreeSet<Key> setA = new TreeSet<>(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class java.util.TreeSet with 250 elements.
     * total size              : 16096
     * element size            : 24
     * data size               : 6000 37%
     * data structure size     : 10096 62%
     * overhead per element    : 40.384 bytes
     * ----footprint---
     * java.util.TreeSet@6f7923a5d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   java.lang.Object
     *          1        48        48   java.util.TreeMap
     *        250        40     10000   java.util.TreeMap$Entry
     *          1        16        16   java.util.TreeMap$KeySet
     *          1        16        16   java.util.TreeSet
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        504               16096   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        TreeSet<Key> setA = new TreeSet<>(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
