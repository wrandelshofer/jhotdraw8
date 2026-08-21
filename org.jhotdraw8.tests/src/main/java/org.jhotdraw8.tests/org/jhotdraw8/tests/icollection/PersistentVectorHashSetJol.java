package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentVectorHashSet;


import java.util.ArrayList;
import java.util.Collections;


public class PersistentVectorHashSetJol extends AbstractJol {

    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.PersistentVectorHashSet with 1000 elements.
    /// total size              : 72816
    /// element size            : 24
    /// data size               : 24000 32%
    /// data structure size     : 48816 67%
    /// overhead per element    : 48.816 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentVectorHashSet@50d0686d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        336        44     14872   [Ljava.lang.Object;
    ///          1       136       136   [[Ljava.lang.Object;
    ///          1        32        32   org.jhotdraw8.icollection.PersistentVectorHashSet
    ///        304        32      9728   org.jhotdraw8.icollection.alt.impl.champset.MutableBitmapIndexedNode
    ///       1000        24     24000   org.jhotdraw8.icollection.alt.impl.champset.SequencedElement
    ///          1        16        16   org.jhotdraw8.icollection.impl.MutabilityOwnership
    ///          1        32        32   org.jhotdraw8.icollection.impl.fingertree.Tree2
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       2644               72816   (total)
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentVectorHashSet<Key> setA = PersistentVectorHashSet.copyOf(data);
        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.SimpleImmutableSequencedSet with 250 elements.
    /// total size              : 19560
    /// element size            : 24
    /// data size               : 6000 30%
    /// data structure size     : 13560 69%
    /// overhead per element    : 54.24 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.SimpleImmutableSequencedSet@2796aeaed footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         88        55      4840   [Ljava.lang.Object;
    ///          1        16        16   org.jhotdraw8.icollection.SimpleImmutableList
    ///          1        32        32   org.jhotdraw8.icollection.SimpleImmutableSequencedSet
    ///          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
    ///         71        32      2272   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
    ///        252        24      6048   org.jhotdraw8.icollection.impl.champ.SequencedElement
    ///         15        16       240   org.jhotdraw8.icollection.impl.champ.Tombstone
    ///          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
    ///          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
    ///        252        24      6048   org.jhotdraw8.icollection.jmh.Key
    ///        683               19560   (total)
    /// </pre>
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentVectorHashSet<Key> setA = PersistentVectorHashSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));


        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
