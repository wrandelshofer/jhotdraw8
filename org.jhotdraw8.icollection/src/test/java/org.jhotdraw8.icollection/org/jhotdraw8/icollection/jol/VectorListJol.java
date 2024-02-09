package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableListJol.
 */
public class VectorListJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableList with 1000 elements.
     * total size              : 28720
     * element size            : 24
     * data size               : 24000 83%
     * data structure size     : 4720 16%
     * overhead per element    : 4.72 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableList@1c33c17bd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         33       141      4656   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.SimpleImmutableList
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1036               28720   (total)
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        VectorList<Key> setA = VectorList.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * cclass org.jhotdraw8.icollection.SimpleImmutableList with 250 elements.
     * total size              : 7240
     * element size            : 24
     * data size               : 6000 82%
     * data structure size     : 1240 17%
     * overhead per element    : 4.96 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableList@ae3540ed footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          9       130      1176   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.SimpleImmutableList
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        262                7240   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        VectorList<Key> setA = VectorList.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
