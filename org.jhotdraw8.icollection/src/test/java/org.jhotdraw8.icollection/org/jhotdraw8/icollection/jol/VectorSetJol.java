package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.VectorSet;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleImmutableSequencedSetJol.
 */
public class VectorSetJol extends AbstractJol {

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSequencedSet with 1000 elements.
     * total size              : 72344
     * element size            : 24
     * data size               : 24000 33%
     * data structure size     : 48344 66%
     * overhead per element    : 48.344 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSequencedSet@42530531d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        326        45     14848   [Ljava.lang.Object;
     *          1        24        24   org.jhotdraw8.icollection.SimpleImmutableList
     *          1        32        32   org.jhotdraw8.icollection.SimpleImmutableSequencedSet
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *        293        32      9376   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *       1000        24     24000   org.jhotdraw8.icollection.impl.champ.SequencedElement
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       2624               72344   (total)
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        VectorSet<Key> setA = VectorSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /**
     * <pre>
     * class org.jhotdraw8.icollection.SimpleImmutableSequencedSet with 250 elements.
     * total size              : 19560
     * element size            : 24
     * data size               : 6000 30%
     * data structure size     : 13560 69%
     * overhead per element    : 54.24 bytes
     * ----footprint---
     * org.jhotdraw8.icollection.SimpleImmutableSequencedSet@2796aeaed footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *         88        55      4840   [Ljava.lang.Object;
     *          1        16        16   org.jhotdraw8.icollection.SimpleImmutableList
     *          1        32        32   org.jhotdraw8.icollection.SimpleImmutableSequencedSet
     *          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
     *         71        32      2272   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
     *        252        24      6048   org.jhotdraw8.icollection.impl.champ.SequencedElement
     *         15        16       240   org.jhotdraw8.icollection.impl.champ.Tombstone
     *          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
     *          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
     *        252        24      6048   org.jhotdraw8.icollection.jmh.Key
     *        683               19560   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        VectorSet<Key> setA = VectorSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removeAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
