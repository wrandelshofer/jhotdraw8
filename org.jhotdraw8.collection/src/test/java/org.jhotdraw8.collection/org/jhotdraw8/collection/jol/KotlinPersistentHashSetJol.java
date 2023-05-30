package org.jhotdraw8.collection.jol;

import kotlinx.collections.immutable.ExtensionsKt;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet with 1000000 elements.
 *
 * total size              : 43618720
 * element size            : 24
 * data size               : 24000000 55%
 * data structure size     : 19618720 44%
 *
 * ----footprint---
 * kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet@6986852d footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *     344264        32  11356360   [Ljava.lang.Object;
 *          1        24        24   kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet
 *     344264        24   8262336   kotlinx.collections.immutable.implementations.immutableSet.TrieNode
 *    1000000        24  24000000   org.jhotdraw8.collection.jmh.Key
 *    1688529            43618720   (total)
 * </pre>
 */
public class KotlinPersistentHashSetJol extends AbstractJol {

    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);

        var mapA = ExtensionsKt.<Key>persistentHashSetOf();
        for (var d : data) {
            mapA = mapA.add(d);
        }
        estimateMemoryUsage(mapA, mapA.iterator().next(), mapA.size());
    }


}
