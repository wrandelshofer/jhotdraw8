package org.jhotdraw8.tests.icollection;

import kotlinx.collections.immutable.ExtensionsKt;


public class KotlinPersistentOrderedSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class kotlinx.collections.immutable.implementations.persistentOrderedSet.PersistentOrderedSet with 1000 elements.
    /// total size              : 71984
    /// element size            : 24
    /// data size               : 24000 33%
    /// data structure size     : 47984 66%
    /// overhead per element    : 47.984 bytes
    /// ----footprint---
    /// kotlinx.collections.immutable.implementations.persistentOrderedSet.PersistentOrderedSet@18bf3d14d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        304        46     14168   [Ljava.lang.Object;
    ///          1        32        32   kotlinx.collections.immutable.implementations.immutableMap.PersistentHashMap
    ///        304        32      9728   kotlinx.collections.immutable.implementations.immutableMap.TrieNode
    ///       1000        24     24000   kotlinx.collections.immutable.implementations.persistentOrderedSet.Links
    ///          1        24        24   kotlinx.collections.immutable.implementations.persistentOrderedSet.PersistentOrderedSet
    ///          1        16        16   kotlinx.collections.immutable.internal.EndOfChain
    ///          1        16        16   kotlinx.collections.immutable.internal.MutabilityOwnership
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       2612               71984   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);

        var setA = ExtensionsKt.<Key>persistentSetOf();
        setA = setA.addAll(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
