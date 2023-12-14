package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <pre>
 * copyOf
 *
 * class org.jhotdraw8.icollection.champ.ChampSet with 1000000 elements.
 *
 * total size              : 46371840
 * element size            : 24
 * data size               : 24000000 51%
 * data structure size     : 22371840 48%
 * </pre>
 * <pre>
 * oneByOne
 *
 * class org.jhotdraw8.icollection.champ.ChampSet with 1000000 elements.
 *
 * total size              : 43618704
 * element size            : 24
 * data size               : 24000000 55%
 * data structure size     : 19618704 44%
 *
 * ----footprint---
 * org.jhotdraw8.icollection.champ.ChampSet@1130520dd footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344264        32  11356360   [Ljava.lang.Object;
 *     344140        24   8259360   org.jhotdraw8.icollection.champ.BitmapIndexedNode
 *          1        32        32   org.jhotdraw8.icollection.champ.ChampSet
 *        123        24      2952   org.jhotdraw8.icollection.champ.HashCollisionNode
 *    1000000        24  24000000   org.jhotdraw8.icollection.jmh.Key
 *    1688528            43618704   (total)
 *
 * Addresses are stable after 1 tries.
 * </pre>
 */
public class ChampSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.champ.ChampSet with 1000 elements.
     * total size              : 47168
     * element size            : 24
     * data size               : 24000 50%
     * data structure size     : 23168 49%
     * ----footprint---
     * org.jhotdraw8.icollection.champ.ChampSet@895e367d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        359        32     11664   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.IdentityObject
     *          1        32        32   org.jhotdraw8.icollection.champ.ChampSet
     *        358        32     11456   org.jhotdraw8.icollection.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1719               47168   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        ChampSet<Key> setA = ChampSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.champ.ChampSet with 250 elements.
     * total size              : 11760
     * element size            : 24
     * data size               : 6000 51%
     * data structure size     : 5760 48%
     * ----footprint---
     * org.jhotdraw8.icollection.champ.ChampSet@1b266842d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         89        32      2880   [Ljava.lang.Object;
     *          2        16        32   org.jhotdraw8.icollection.IdentityObject
     *          1        32        32   org.jhotdraw8.icollection.champ.ChampSet
     *         88        32      2816   org.jhotdraw8.icollection.champ.MutableBitmapIndexedNode
     *        250        24      6000   org.jhotdraw8.icollection.jmh.Key
     *        430               11760   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        ChampSet<Key> setA = ChampSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
