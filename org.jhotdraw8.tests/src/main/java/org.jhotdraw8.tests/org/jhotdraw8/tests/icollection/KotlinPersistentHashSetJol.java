package org.jhotdraw8.tests.icollection;

import kotlinx.collections.immutable.ExtensionsKt;

public class KotlinPersistentHashSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet with 1000 elements.
    /// total size              : 41696
    /// element size            : 24
    /// data size               : 24000 57%
    /// data structure size     : 17696 42%
    /// overhead per element    : 17.696 bytes
    /// ----footprint---
    /// kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet@2de8284bd footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        304        34     10360   [Ljava.lang.Object;
    ///          1        24        24   kotlinx.collections.immutable.implementations.immutableSet.PersistentHashSet
    ///        304        24      7296   kotlinx.collections.immutable.implementations.immutableSet.TrieNode
    ///          1        16        16   kotlinx.collections.immutable.internal.MutabilityOwnership
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1610               41696   (total)
    /// ```

    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);

        var setA = ExtensionsKt.<Key>persistentHashSetOf();
        setA = setA.addAll(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
