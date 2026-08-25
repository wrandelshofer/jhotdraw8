package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentHashSet;

import java.util.ArrayList;
import java.util.Collections;

public class PersistentHashSetJol extends AbstractJol {

    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class org.jhotdraw8.icollection.PersistentHashSet with 1000 elements.
    /// total size              : 41696
    /// element size            : 24
    /// data size               : 24000 57%
    /// data structure size     : 17696 42%
    /// overhead per element    : 17.696 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentHashSet@3e57cd70d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        304        34     10360   [Ljava.lang.Object;
    ///          1        24        24   org.jhotdraw8.icollection.PersistentHashSet
    ///          1        16        16   org.jhotdraw8.icollection.impl.MutabilityOwnership
    ///        304        24      7296   org.jhotdraw8.icollection.impl.champset.MutableTrieNode
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1610               41696   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        PersistentHashSet<Key> setA = PersistentHashSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// ```
    /// class org.jhotdraw8.icollection.ChampSet with 1000 elements.
    /// total size              : 41248
    /// element size            : 24
    /// data size               : 24000 58%
    /// data structure size     : 17248 41%
    /// overhead per element    : 17.248 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.ChampSet@4241e0f4d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        293        34     10192   [Ljava.lang.Object;
    ///          1        24        24   org.jhotdraw8.icollection.ChampSet
    ///        293        24      7032   org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode
    ///       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
    ///       1587               41248   (total)
    /// ```

    public void estimateMemoryUsageAddingOneByOne(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        PersistentHashSet<Key> setA = PersistentHashSet.of();
        for (var d : data) {
            setA = setA.adding(d);
        }
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// ```
    /// class org.jhotdraw8.icollection.ChampSet with 250 elements.
    /// total size              : 11000
    /// element size            : 24
    /// data size               : 6000 54%
    /// data structure size     : 5000 45%
    /// overhead per element    : 20.0 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.ChampSet@95e33ccd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///         74        34      2576   [Ljava.lang.Object;
    ///          1        24        24   org.jhotdraw8.icollection.ChampSet
    ///          2        16        32   org.jhotdraw8.icollection.impl.IdentityObject
    ///         74        32      2368   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
    ///        250        24      6000   org.jhotdraw8.icollection.jmh.Key
    ///        401               11000   (total)
    /// ```

    public void estimateMemoryUsageAfter75PercentRandomRemoves(int size) {
        final int mask = ~64;
        var data = generateSet(size, mask);
        PersistentHashSet<Key> setA = PersistentHashSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
