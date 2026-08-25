package org.jhotdraw8.tests.icollection;


import java.util.HashMap;

public class JavaHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class java.util.HashMap with 1000 elements.
    /// total size              : 88272
    /// element size            : 48
    /// data size               : 48000 54%
    /// data structure size     : 40272 45%
    /// overhead per element    : 40.272 bytes
    /// ----footprint---
    /// java.util.HashMap@2c039ac6d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1      8208      8208   [Ljava.util.HashMap$Node;
    ///          1        48        48   java.util.HashMap
    ///          1        16        16   java.util.HashMap$EntrySet
    ///       1000        32     32000   java.util.HashMap$Node
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3003               88272   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);

        HashMap<Key, Value> mapA = new HashMap<>(data);
        AbstractJol.estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
