package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.PersistentHashMap;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

/// SimpleImmutableMapJol.
public class PersistentHashMapJol extends AbstractJol {
    public void main() {
        var test = new PersistentHashMapJol();
        int size = 1000;
        test.estimateMemoryUsage(size);
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.PersistentHashMap with 1000 elements.
    /// total size              : 70808
    /// element size            : 48
    /// data size               : 48000 67%
    /// data structure size     : 22808 32%
    /// overhead per element    : 22.808 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentHashMap@bd8db5ad footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        283        48     13712   [Ljava.lang.Object;
    ///          1        24        24   org.jhotdraw8.icollection.PersistentHashMap
    ///          1        16        16   org.jhotdraw8.icollection.impl.MutabilityOwnership
    ///        283        32      9056   org.jhotdraw8.icollection.impl.champmap.TrieNode
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Key
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Value
    ///       2568               70808   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        PersistentHashMap<Key, Value> mapA = PersistentHashMap.copyOf(data);
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.ChampMap with 1000 elements.
    /// total size              : 68528
    /// element size            : 48
    /// data size               : 48000 70%
    /// data structure size     : 20528 29%
    /// overhead per element    : 20.528 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.ChampMap@e383572d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        283        48     13712   [Ljava.lang.Object;
    ///          1        24        24   org.jhotdraw8.icollection.ChampMap
    ///        283        24      6792   org.jhotdraw8.icollection.alt.impl.champmap.BitmapIndexedNode
    ///       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
    ///       1000        24     24000   org.jhotdraw8.icollection.jmh.Value
    ///       2567               68528   (total)
    /// </pre>
    public void estimateMemoryUsageNoBulkOperations(int size) {
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10);
        PersistentHashMap<Key, Value> mapA = PersistentHashMap.of();
        for (var e : data.entrySet()) {
            mapA = mapA.putting(e.getKey(), e.getValue());
        }
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /// class org.jhotdraw8.icollection.SimpleImmutableMap with 1000 elements.
    ///
    /// both versions:
    /// total size              : 71256
    ///
    /// mapA:
    /// total size              : 70808
    ///
    /// mapB:
    /// total size              : 70792
    ///
    /// Difference: 71256 - 70808 = 448 bytes
    public void estimateMemoryUsageForANewVersion(int size) {
        final int mask = -1;//~64;
        var data = generateMap(size, mask, size * 10L);
        PersistentHashMap<Key, Value> mapA = PersistentHashMap.copyOf(data);
        Key updatedKey = data.keySet().iterator().next();
        PersistentHashMap<Key, Value> mapB = mapA.putting(updatedKey, new Value(mapA.get(updatedKey).value + 1, -1));
        AbstractMap.SimpleImmutableEntry<PersistentHashMap<Key, Value>, PersistentHashMap<Key, Value>> twoVersions = new AbstractMap.SimpleImmutableEntry<>(mapA, mapB);
        System.out.println("\nboth versions:");
        estimateMemoryUsage(twoVersions, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapA:");
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
        System.out.println("\nmapB:");
        estimateMemoryUsage(mapB, mapA.iterator().next(), mapA.size());
    }

    /// <pre>
    /// class org.jhotdraw8.icollection.champ.SimpleImmutableMap with 250 elements.
    /// total size              : 23224
    /// element size            : 48
    /// data size               : 12000 51%
    /// data structure size     : 11224 48%
    /// ----footprint---
    /// org.jhotdraw8.icollection.champ.SimpleImmutableMap@651aed93d footprint:
    /// COUNT       AVG       SUM   DESCRIPTION
    ///  79        33      2664   [Ljava.lang.Object;
    /// 250        24      6000   java.util.AbstractMap$SimpleImmutableEntry
    ///   2        16        32   org.jhotdraw8.icollection.IdentityObject
    ///   1        32        32   org.jhotdraw8.icollection.champ.SimpleImmutableMap
    ///  78        32      2496   org.jhotdraw8.icollection.champ.MutableBitmapIndexedNode
    /// 500        24     12000   org.jhotdraw8.icollection.jmh.Key
    /// 910               23224   (total)
    /// </pre>
    public void estimateMemoryUsageAfter75PercentRandomRemoves(int size) {
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);
        PersistentHashMap<Key, Value> mapA = PersistentHashMap.copyOf(data);

        ArrayList<Key> keys = new ArrayList<>(data.keySet());
        Collections.shuffle(keys);
        mapA = mapA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));

        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
