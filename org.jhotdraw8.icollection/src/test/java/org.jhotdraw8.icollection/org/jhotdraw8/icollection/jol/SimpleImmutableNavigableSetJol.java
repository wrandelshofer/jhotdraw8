package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.SimpleImmutableNavigableSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableNavigableSetJol.
 */
public class SimpleImmutableNavigableSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableNavigableSet with 1000 elements.
     * total size              : 56056
     * element size            : 24
     * data size               : 24000 42%
     * data structure size     : 32056 57%
     * overhead per element    : 32.056 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableNavigableSet@67304a40d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableSet
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *       1000        32     32000   org.jhotdraw8.icollection.impl.redblack.Node
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       2003               56056   (total)
     * </pre>
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableNavigableSet with 1 elements.
     * total size              : 112
     * element size            : 24
     * data size               : 24 21%
     * data structure size     : 88 78%
     * overhead per element    : 88.0 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableNavigableSet@51133c06d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableSet
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *          1        32        32   org.jhotdraw8.icollection.impl.redblack.Node
     *          1        24        24   org.jhotdraw8.icollection.jmh.Key
     *          5                 112   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        SimpleImmutableNavigableSet<Key> setA = SimpleImmutableNavigableSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableNavigableSet with 250 elements.
     * total size              : 14056
     * element size            : 24
     * data size               : 6000 42%
     * data structure size     : 8056 57%
     * overhead per element    : 32.224 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableNavigableSet@f107c50d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        16        16   org.jhotdraw8.icollection.NaturalComparator
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableSet
     *          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
     *        250        32      8000   org.jhotdraw8.icollection.impl.redblack.Node
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        503               14056   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        SimpleImmutableNavigableSet<Key> setA = SimpleImmutableNavigableSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
