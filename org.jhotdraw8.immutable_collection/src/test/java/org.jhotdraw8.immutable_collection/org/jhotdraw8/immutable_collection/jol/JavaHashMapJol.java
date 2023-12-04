package org.jhotdraw8.immutable_collection.jol;

import org.jhotdraw8.immutable_collection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * <pre>
 * class java.util.HashMap with 1000000 elements.
 * total size              : 88388688
 * element size            : 48
 * data size               : 48000000 54%
 * data structure size     : 40388688 45%
 * ----footprint---
 * java.util.HashMap@51a9ad5ed footprint:
 *      COUNT       AVG       SUM   DESCRIPTION
 *          1   8388624   8388624   [Ljava.util.HashMap$Node;
 *          1        48        48   java.util.HashMap
 *          1        16        16   java.util.HashMap$EntrySet
 *    1000000        32  32000000   java.util.HashMap$Node
 *    2000000        24  48000000   org.jhotdraw8.immutable_collection.jmh.Key
 *    3000003            88388688   (total)
 * </pre>
 */
public class JavaHashMapJol extends AbstractJol {

    @Test
    @Disabled

    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateMap(size, mask);

        HashMap<Key, Key> mapA = new HashMap<>(data);
        estimateMemoryUsage(mapA, mapA.entrySet().iterator().next(), mapA.size());
    }


}
