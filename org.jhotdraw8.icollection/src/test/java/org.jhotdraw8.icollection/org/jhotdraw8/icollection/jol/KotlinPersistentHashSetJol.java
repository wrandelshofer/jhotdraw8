package org.jhotdraw8.icollection.jol;

import kotlinx.collections.immutable.ExtensionsKt;
import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *KotlinPersistentHashSetJol
 */
public class KotlinPersistentHashSetJol extends AbstractJol {

    /**
     * <pre>
     * class kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet with 1000 elements.
     * total size              : 41264
     * element size            : 24
     * data size               : 24000 58%
     * data structure size     : 17264 41%
     * overhead per element    : 17.264 bytes
     * ----footprint---
     * kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet@63f259c3d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *        293        34     10192   [Ljava.lang.Object;
     *          1        24        24   kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet
     *        293        24      7032   kotlinx.collections.immutable.implementations.immutableSet.TrieNode
     *          1        16        16   kotlinx.collections.immutable.internal.MutabilityOwnership
     *       1000        24     24000   org.jhotdraw8.icollection.jmh.Key
     *       1588               41264   (total)
     * </pre>
     * <pre>
     * class kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet with 1 elements.
     * total size              : 112
     * element size            : 24
     * data size               : 24 21%
     * data structure size     : 88 78%
     * overhead per element    : 88.0 bytes
     * ----footprint---
     * kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet@63f259c3d footprint:
     *      COUNT       AVG       SUM   DESCRIPTION
     *          1        24        24   [Ljava.lang.Object;
     *          1        24        24   kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet
     *          1        24        24   kotlinx.collections.immutable.implementations.immutableSet.TrieNode
     *          1        16        16   kotlinx.collections.immutable.internal.MutabilityOwnership
     *          1        24        24   org.jhotdraw8.icollection.jmh.Key
     *          5                 112   (total)
     * </pre>
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = -1;//~64;
        var data = generateSet(size, mask);

        var setA = ExtensionsKt.<Key>persistentHashSetOf();
        setA = setA.addAll(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
