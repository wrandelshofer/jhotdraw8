package org.jhotdraw8.icollection.impl.rrbnaive;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.Assert.assertEquals;

public class RrbTreeTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 1000, 10_000})
    public void shouldGenerateTree(int size) {
        RrbTree<Integer> tree = RrbTree.initSparse(size, 7, new MutabilityOwnership());
        assertEquals(size, tree.size());

        for (int i = size - 1; i >= 0; i--) {
            assertEquals((Object) 7, tree.getAt(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 1000, 10_000})
    public void shouldSetValue(int size) {
        RrbTree<Integer> tree = RrbTree.initSparse(size, -1, new MutabilityOwnership());
        assertEquals(size, tree.size());

        MutabilityOwnership owner = new MutabilityOwnership();
        for (int i = size - 1; i >= 0; i--) {
            tree = tree.settingAt(i, i, owner);
            assertEquals((Object) i, tree.getAt(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 1000, 10_000})
    public void shouldAddLast(int size) {
        RrbTree<Integer> tree = RrbTree.empty();
        MutabilityOwnership owner = new MutabilityOwnership();
        for (int i = 0; i < size; i++) {
            tree = tree.addingLast(i, owner);
        }
        for (int i = size - 1; i >= 0; i--) {
            int actual = (Integer) tree.getAt(i);
            assertEquals((Object) i, actual);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 1000, 10_000})
    public void shouldRemoveLast(int size) {
        RrbTree<Integer> tree = RrbTree.initSparse(size, 7, new MutabilityOwnership());
        MutabilityOwnership owner = new MutabilityOwnership();
        for (int i = size - 1; i >= 0; i--) {
            tree = tree.settingAt(i, i, owner);
            assertEquals((Object) i, tree.getAt(i));
        }

        for (int i = size - 1; i >= 0; i--) {
            tree = tree.removingLast(owner);
            for (int j = 0; j < i; j++) {
                int actual = (Integer) tree.getAt(j);
                assertEquals((Object) j, actual);
            }
        }
    }


}