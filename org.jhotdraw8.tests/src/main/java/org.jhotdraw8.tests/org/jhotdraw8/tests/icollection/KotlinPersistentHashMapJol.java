package org.jhotdraw8.tests.icollection;

import kotlinx.collections.immutable.ExtensionsKt;

public class KotlinPersistentHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap with 1000 elements.
    /// total size              : 87688
    /// element size            : 48
    /// data size               : 48000 54%
    /// data structure size     : 39688 45%
    /// overhead per element    : 39.688 bytes
    /// ----footprint---
    /// kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap@543c6f6dd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        588        35     20840   [Ljava.lang.Object;
    ///          1        32        32   kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap
    ///        588        32     18816   kotlinx.collections.immutable.implementations.immutableMap.TrieNode
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3177               87688   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);

        var mapA = ExtensionsKt.persistentHashMapOf();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        AbstractJol.estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
