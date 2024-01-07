package org.jhotdraw8.icollection.jol;

import kotlinx.collections.immutable.ExtensionsKt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * KotlinPersistentHashMapJol
 */
public class KotlinPersistentHashMapJol extends AbstractJol {

    /**
     * <pre>
     * class kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap with 1000 elements.
     * total size              : 74400
     * element size            : 48
     * data size               : 48000 64%
     * data structure size     : 26400 35%
     * overhead per element    : 26.4 bytes
     * ----footprint---
     * kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap@5be46f9dd footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        350        43     15168   [Ljava.lang.Object;
     *          1        32        32   kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap
     *        350        32     11200   kotlinx.collections.immutable.implementations.immutableMap.TrieNode
     *       2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     *       2701               74400   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);

        var mapA = ExtensionsKt.persistentHashMapOf();
        for (var d : data.entrySet()) {
            mapA = mapA.put(d.getKey(), d.getValue());
        }
        estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
