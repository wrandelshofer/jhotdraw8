package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * JavaHashMapJol.
 */
public class JavaHashMapJol extends AbstractJol {

    /**
     * class java.util.HashMap with 1000 elements.
     * total size              : 88272
     * element size            : 48
     * data size               : 48000 54%
     * data structure size     : 40272 45%
     * ----footprint---
     * java.util.HashMap@6f7923a5d footprint:
     * COUNT       AVG       SUM   DESCRIPTION
     * 1      8208      8208   [Ljava.util.HashMap$Node;
     * 1        48        48   java.util.HashMap
     * 1        16        16   java.util.HashMap$EntrySet
     * 1000        32     32000   java.util.HashMap$Node
     * 2000        24     48000   org.jhotdraw8.icollection.jmh.Key
     * 3003               88272   (total)
     */
    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask, size * 10);

        HashMap<Key, Value> mapA = new HashMap<>(data);
        estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
