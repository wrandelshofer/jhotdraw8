package org.jhotdraw8.tests.icollection;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class JavaLinkedHashMapJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class java.util.LinkedHashMap with 1000 elements.
    /// total size              : 96296
    /// element size            : 48
    /// data size               : 48000 49%
    /// data structure size     : 48296 50%
    /// overhead per element    : 48.296 bytes
    /// ----footprint---
    /// java.util.LinkedHashMap@2c039ac6d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          1      8208      8208   [Ljava.util.HashMap$Node;
    ///          1        64        64   java.util.LinkedHashMap
    ///       1000        40     40000   java.util.LinkedHashMap$Entry
    ///          1        24        24   java.util.LinkedHashMap$LinkedEntrySet
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Value
    ///       3003               96296   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateMap(size, mask, size * 10);

        HashMap<Key, Value> mapA = new LinkedHashMap<>(data);
        AbstractJol.estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
