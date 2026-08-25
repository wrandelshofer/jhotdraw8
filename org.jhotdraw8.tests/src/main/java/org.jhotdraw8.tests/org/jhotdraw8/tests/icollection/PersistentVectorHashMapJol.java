package org.jhotdraw8.tests.icollection;

//import org.jhotdraw8.icollection.PersistentVectorHashMap;


public class PersistentVectorHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class org.jhotdraw8.icollection.PersistentVectorHashMap with 1000 elements.
    /// total size              : 95696
    /// element size            : 48
    /// data size               : 48000 50%
    /// data structure size     : 47696 49%
    /// overhead per element    : 47.696 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.PersistentVectorHashMap@5ed828dd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        315        45     14424   [Ljava.lang.Object;
    ///          1       136       136   [[Ljava.lang.Object;
    ///          1        32        32   org.jhotdraw8.icollection.PersistentVectorHashMap
    ///        283        32      9056   org.jhotdraw8.icollection.alt.impl.champset.MutableBitmapIndexedNode
    ///       1000        24     24000   org.jhotdraw8.icollection.alt.impl.champset.SequencedEntry
    ///          1        16        16   org.jhotdraw8.icollection.impl.MutabilityOwnership
    ///          1        32        32   org.jhotdraw8.icollection.impl.fingertree.Tree2
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3602               95696   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = -1;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        // PersistentVectorHashMap<Key, Value> mapA = PersistentVectorHashMap.copyOf(data);
        // AbstractJol.estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }

    /// ```
    /// class org.jhotdraw8.icollection.ChampVectorMap with 250 elements.
    /// total size              : 26832
    /// element size            : 48
    /// data size               : 12000 44%
    /// data structure size     : 14832 55%
    /// overhead per element    : 59.328 bytes
    /// ----footprint---
    /// org.jhotdraw8.icollection.ChampVectorMap@32c726eed footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        115        47      5432   [Ljava.lang.Object;
    ///          1        32        32   org.jhotdraw8.icollection.ChampVectorMap
    ///          1        16        16   org.jhotdraw8.icollection.VectorList
    ///          1        16        16   org.jhotdraw8.icollection.impl.IdentityObject
    ///         95        32      3040   org.jhotdraw8.icollection.impl.champ.MutableBitmapIndexedNode
    ///          3        24        72   org.jhotdraw8.icollection.impl.champ.MutableHashCollisionNode
    ///        250        24      6000   org.jhotdraw8.icollection.impl.champ.SequencedEntry
    ///         11        16       176   org.jhotdraw8.icollection.impl.champ.Tombstone
    ///          1        16        16   org.jhotdraw8.icollection.impl.vector.ArrayType$ObjectArrayType
    ///          1        32        32   org.jhotdraw8.icollection.impl.vector.BitMappedTrie
    ///        250        24      6000   org.jhotdraw8.icollection.jmh.Key
    ///        250        24      6000   org.jhotdraw8.icollection.jmh.Value
    ///        979               26832   (total)
    /// ```
    public void estimateMemoryUsageAfter75PercentRandomRemoves() {
        int size = 1000;
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);
        // PersistentVectorHashMap<Key, Value> mapA = PersistentVectorHashMap.copyOf(data);
        // ArrayList<Key> keys = new ArrayList<>(data.keySet());
        // Collections.shuffle(keys);
        // mapA = mapA.removingAll(keys.subList(0, (int) (keys.size() * 0.75)));
        // AbstractJol.estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }
}
