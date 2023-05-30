package org.jhotdraw8.collection.jol;

import kotlinx.collections.immutable.ExtensionsKt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap with 1000000 elements.
 * total size              : 74035176
 * element size            : 48
 * data size               : 48000000 64%
 * data structure size     : 26035176 35%
 *
 * kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap@5a37d3edd footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344064        43  15025096   [Ljava.lang.Object;
 *          1        32        32   kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap
 *     344064        32  11010048   kotlinx.collections.immutable.implementations.immutableMap.TrieNode
 *    2000000        24  48000000   org.jhotdraw8.collection.jmh.Key
 *    2688129            74035176   (total)
 * </pre>
 */
public class KotlinPersistentHashMapJol extends AbstractJol {

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
