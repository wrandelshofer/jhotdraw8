package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.SimpleImmutableSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableSetJol.
 */
public class SimpleImmutableSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSet with 1000 elements.
     * total size              : 43608
     * element size            : 24
     * data size               : 24000 55%
     * data structure size     : 19608 44%
     * overhead per element    : 19.608 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSet@60410cdd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        34     10192   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableSet
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        293        32      9376   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1588               43608   (total)
     * </pre>
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSet with 1 elements.
     * total size              : 120
     * element size            : 24
     * data size               : 24 20%
     * data structure size     : 96 80%
     * overhead per element    : 96.0 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSet@95e33ccd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        24        24   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableSet
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *          1        32        32   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *          1        24        24   org.jhotdraw8.icollection.jmh.Key
     *          5                 120   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAddAll() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        SimpleImmutableSet<Key> setA = SimpleImmutableSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSet with 1000 elements.
     * total size              : 41248
     * element size            : 24
     * data size               : 24000 58%
     * data structure size     : 17248 41%
     * overhead per element    : 17.248 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSet@4241e0f4d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        34     10192   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableSet
     *        293        24      7032   org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1587               41248   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAddOneByOne() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        SimpleImmutableSet<Key> setA = SimpleImmutableSet.of();
        for (var d : data) {
            setA = setA.add(d);
        }
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSet with 250 elements.
     * total size              : 11000
     * element size            : 24
     * data size               : 6000 54%
     * data structure size     : 5000 45%
     * overhead per element    : 20.0 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSet@95e33ccd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         74        34      2576   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableSet
     *          2        16        32   org.jhotdraw8.icollection.impl.IdentityObject
     *         74        32      2368   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        401               11000   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        SimpleImmutableSet<Key> setA = SimpleImmutableSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
