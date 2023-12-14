package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.HashSet;

/**
 * <pre>
 * class scala.collection.immutable.HashSet with 1000000 elements.
 *
 * total size              : 55957248
 * element size            : 24
 * data size               : 24000000 42%
 * data structure size     : 31957248 57%
 * </pre>
 */
public class ScalaHashSetJol extends AbstractJol {

    @Test
    @Disabled
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        var b = HashSet.<Key>newBuilder();
        for (var d : data) {
            b.addOne(d);
        }
        HashSet<Key> setA = b.result();
        Key head = setA.head();
        estimateMemoryUsage(setA,
                head, setA.size());
    }


}
