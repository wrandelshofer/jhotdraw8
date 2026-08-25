package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentTreeSet;


import java.util.ArrayList;
import java.util.Collections;


public class PersistentTreeSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class org.jhotdraw8.icollection.PersistentTreeSet with 1000 elements.
    /// total size              : 56056
    /// element size            : 24
    /// data size               : 24000 42%
    /// data structure size     : 32056 57%
    /// overhead per element    : 32.056 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentTreeSet@5bfbf16fd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1        16        16   org.jhotdraw8.icollection.NaturalComparator
    ///          1        24        24   org.jhotdraw8.icollection.PersistentTreeSet
    ///          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
    ///       1000        32     32000   org.jhotdraw8.icollection.impl.redblack.Node
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       2003               56056   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentTreeSet<Key> setA = PersistentTreeSet.copyOf(data);
        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// ```
    /// class org.jhotdraw8.icollection.SimpleImmutableNavigableSet with 250 elements.
    /// total size              : 14056
    /// element size            : 24
    /// data size               : 6000 42%
    /// data structure size     : 8056 57%
    /// overhead per element    : 32.224 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.SimpleImmutableNavigableSet@f107c50d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1        16        16   org.jhotdraw8.icollection.NaturalComparator
    ///          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableSet
    ///          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
    ///        250        32      8000   org.jhotdraw8.icollection.impl.redblack.Node
    ///        250        24      6000   org.jhotdraw8.icollection.jmh.Key
    ///        503               14056   (total)
    /// ```
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1_000;
        final int mask = ~64;
        var data = AbstractJol.generateSet(size, mask);
        PersistentTreeSet<Key> setA = PersistentTreeSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));


        AbstractJol.estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
