package org.jhotdraw8.icollection.impl.fingertree;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FingerTreeTest {
    @Disabled
    @ParameterizedTest
    @ValueSource(ints = {32 * 32 + 32 + 1})
    public void shouldReduceSliceCount(int size) {
        FingerTree<Integer> tree = new FingerTreeBuilder<Integer>().initSparse(size, 1).build();
        int sliceCount = tree.getSliceCount();
        FingerTree<Integer> tree1 = tree;
        FingerTree<Integer> tree2 = tree;
        while (tree1.getSliceCount() == sliceCount) {
            tree1 = (FingerTree<Integer>) tree1.removeFirst().tree();
        }
        while (tree2.getSliceCount() == sliceCount) {
            tree2 = (FingerTree<Integer>) tree2.removeFirst().tree();
        }
        IO.println("tree=" + tree.getClass().getSimpleName());
        IO.println("tree1=" + tree1.size() + " tree2=" + tree2.size() + " tree=" + tree.size());
        int sliceCount1 = tree1.getSliceCount();
        int sliceCount2 = tree2.getSliceCount();
        assertTrue(sliceCount1 < sliceCount,
                "must have reduced slice count, newSlice=" + sliceCount1 + ", sliceCount=" + sliceCount);
        assertEquals(sliceCount1, sliceCount2);

    }

}