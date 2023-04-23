package org.jhotdraw8.collection.jol;

import org.jhotdraw8.collection.SequencedChampSet;
import org.jhotdraw8.collection.jmh.Key;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * class org.jhotdraw8.collection.champ.SequencedChampSet with 1000000 elements.
 *
 * total size              : 87791448
 * element size            : 24
 * data size               : 24000000 27%
 * data structure size     : 63791448 72%
 * </pre>
 */
public class SequencedChampSetJol extends AbstractJol {

    @Test
    public void estimateMemoryUsage() {
        int size = 1_000;
        final int mask = ~64;
        var data = generateSet(size, mask);
        SequencedChampSet<Key> setA = SequencedChampSet.copyOf(data);
        estimateMemoryUsage(setA, setA.iterator().next(), setA.size());
    }


}
