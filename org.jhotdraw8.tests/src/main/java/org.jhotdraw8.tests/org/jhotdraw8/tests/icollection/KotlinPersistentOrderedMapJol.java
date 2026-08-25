package org.jhotdraw8.tests.icollection;

import kotlinx.collections.immutable.ExtensionsKt;


public class KotlinPersistentOrderedMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class kotlinx.collections.immutable.implementations.persistentOrderedMap.PersistentOrderedMap with 1000 elements.
    /// total size              : 111736
    /// element size            : 48
    /// data size               : 48000 42%
    /// data structure size     : 63736 57%
    /// overhead per element    : 63.736 bytes
    /// ----footprint---
    /// kotlinx.collections.immutable.implementations.persistentOrderedMap.PersistentOrderedMap@544a2ea6d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        588        35     20840   [Ljava.lang.Object;
    ///          1        32        32   kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap
    ///        588        32     18816   kotlinx.collections.immutable.implementations.immutableMap.TrieNode
    ///       1000        24     24000   kotlinx.collections.immutable.implementations.persistentOrderedMap.LinkedValue
    ///          1        32        32   kotlinx.collections.immutable.implementations.persistentOrderedMap.PersistentOrderedMap
    ///          1        16        16   kotlinx.collections.immutable.internal.EndOfChain
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       4179              111736   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);

        var mapA = ExtensionsKt.persistentMapOf();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        AbstractJol.estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
