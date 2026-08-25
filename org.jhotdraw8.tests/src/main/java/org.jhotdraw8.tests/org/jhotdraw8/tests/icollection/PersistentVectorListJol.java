package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentVectorList;

import java.util.ArrayList;
import java.util.Collections;

/// SimpleImmutableListJol.
public class PersistentVectorListJol extends AbstractJol {
    void main() {
        var test = new PersistentHashSetJol();
        int size = 1000;
        test.estimateMemoryUsage(size);
    }

    /// ```
    /// class org.jhotdraw8.icollection.impl.fingertree.Tree2 with 1000 elements.
    /// total size              : 28680
    /// element size            : 24
    /// data size               : 24000 83%
    /// data structure size     : 4680 16%
    /// overhead per element    : 4.68 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.impl.fingertree.Tree2@3cb1ffe6d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         32       141      4512   [Ljava.lang.Object;
    ///          1       136       136   [[Ljava.lang.Object;
    ///          1        32        32   org.jhotdraw8.icollection.impl.fingertree.Tree2
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Key
    ///       1034               28680   (total)
    /// ```

    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentVectorList<Key> setA = PersistentVectorList.copyOf(data);
        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// ```
    /// cclass org.jhotdraw8.icollection.SimpleImmutableList with 250 elements.
    /// total size              : 7240
    /// element size            : 24
    /// data size               : 6000 82%
    /// data structure size     : 1240 17%
    /// overhead per element    : 4.96 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.SimpleImmutableList@ae3540ed footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          9       130      1176   [Ljava.lang.Object;
    ///          1        16        16   org.jhotdraw8.icollection.SimpleImmutableList
    ///          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
    ///          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
    ///        250        24      6000   org.jhotdraw8.icollection.jmh.Key
    ///        262                7240   (total)
    /// ```

    public void estimateMemoryUsageAfter75PercentRandomRemoves(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentVectorList<Key> setA = PersistentVectorList.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));


        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
