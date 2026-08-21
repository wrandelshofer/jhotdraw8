package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentTreeMap;


import java.util.ArrayList;
import java.util.Collections;

public class PersistentTreeMapJol extends AbstractJol {
    void main() {
        var test = new PersistentTreeMapJol();
        int size = 1000;
        test.estimateMemoryUsage(size);
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.PersistentTreeMap with 1000 elements.
    /// total size              : 80056
    /// element size            : 48
    /// data size               : 48000 59%
    /// data structure size     : 32056 40%
    /// overhead per element    : 32.056 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentTreeMap@1500955ad footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1        16        16   org.jhotdraw8.icollection.NaturalComparator
    ///          1        24        24   org.jhotdraw8.icollection.PersistentTreeMap
    ///          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
    ///       1000        32     32000   org.jhotdraw8.icollection.impl.redblack.Node
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3003               80056   (total)
    /// </pre>


    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        PersistentTreeMap<Key, Value> mapA = PersistentTreeMap.copyOf(data);
        AbstractJol.estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.SimpleImmutableNavigableMap with 250 elements.
    /// total size              : 22056
    /// element size            : 48
    /// data size               : 12000 54%
    /// data structure size     : 10056 45%
    /// overhead per element    : 40.224 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.SimpleImmutableNavigableMap@4b213651d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1        16        16   org.jhotdraw8.icollection.NaturalComparator
    ///          1        24        24   org.jhotdraw8.icollection.SimpleImmutableNavigableMap
    ///          1        16        16   org.jhotdraw8.icollection.impl.redblack.Empty
    ///        250        40     10000   org.jhotdraw8.icollection.impl.redblack.Node
    ///        500        24     12000   org.jhotdraw8.icollection.jmh.Key
    ///        753               22056   (total)
    /// </pre>


    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1000;
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        PersistentTreeMap<Key, Value> mapA = PersistentTreeMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));

        AbstractJol.estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
