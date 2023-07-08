package org.jhotdraw8.pcollection.jol;

import io.vavr.collection.LinkedHashSet;
import org.jhotdraw8.pcollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class io.vavr.collection.LinkedHashSet with 1000000 elements.
 *
 * total size              : 115880752
 * element size            : 24
 * data size               : 24000000 20%
 * data structure size     : 91880752 79%
 * </pre>
 */
public class VavrLinkedHashSetJol extends AbstractJol {

    @Disabled
    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        var setA = LinkedHashSet.<Key>empty();
        for (var d : data) {
            setA = setA.add(d);
        }
        Key head = setA.head();
        estimateMemoryUsage(setA, head, setA.size());
    }


}
