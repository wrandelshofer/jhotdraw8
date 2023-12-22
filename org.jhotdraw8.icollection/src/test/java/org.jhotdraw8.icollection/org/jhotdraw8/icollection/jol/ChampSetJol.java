package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * ChampSetJol.
 */
public class ChampSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.ChampSet with 1000 elements.
     * total size              : 47192
     * element size            : 24
     * data size               : 24000 50%
     * data structure size     : 23192 49%
     * ----footprint---
     * org.jhotdraw8.icollection.ChampSet@babafc2d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        359        32     11664   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.ChampSet
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        359        32     11488   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1720               47192   (total)
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
