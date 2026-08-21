package org.jhotdraw8.tests.icollection;

import org.jhotdraw8.icollection.PersistentLinkedHashSet;

import java.util.ArrayList;
import java.util.Collections;

public class PersistentLinkedHashSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }


    /// <pre>
    /// class org.jhotdraw8.icollection.PersistentLinkedHashSet with 1000 elements.
    /// total size              : 52232
    /// element size            : 24
    /// data size               : 24000 45%
    /// data structure size     : 28232 54%
    /// overhead per element    : 28.232 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentLinkedHashSet@58c1670bd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        306        60     18424   [Ljava.lang.Object;
    ///          1        16        16   java.lang.Object
    ///          1        32        32   org.jhotdraw8.icollection.PersistentLinkedHashSet
    ///          2        16        32   org.jhotdraw8.icollection.impl.MutabilityOwnership
    ///        304        32      9728   org.jhotdraw8.icollection.impl.champlinked.TrieNode
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1614               52232   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        PersistentLinkedHashSet<Key> setA = PersistentLinkedHashSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// <pre>
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
    /// </pre>

    public void estimateMemoryUsageAddingOneByOne(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        PersistentLinkedHashSet<Key> setA = PersistentLinkedHashSet.of();
        for (var d : data) {
            setA = setA.adding(d);
        }
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }

    /// <pre>
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
    /// </pre>

    public void estimateMemoryUsageAfter75PercentRandomRemoves(int size) {
        final int mask = ~64;
        var data = generateSet(size, mask);
        PersistentLinkedHashSet<Key> setA = PersistentLinkedHashSet.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data);
        Collections.shuffle(keys);
        setA = setA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));


        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
