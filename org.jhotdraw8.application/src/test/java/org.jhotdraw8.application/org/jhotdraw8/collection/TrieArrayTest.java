/*
 * @(#)TrieArrayTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.jhotdraw8.collection.TrieListHelper.M;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class TrieArrayTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsAddAndRemove() {
        return Arrays.asList(
                dynamicTest("empty", () -> doTest(0)),
                dynamicTest("height 0 barely filled", () -> doTest(1)),
                dynamicTest("height 0 almost full", () -> doTest(M - 1)),
                dynamicTest("height 0 full", () -> doTest(M)),
                dynamicTest("height 1 barely filled", () -> doTest(M + 1)),
                dynamicTest("height 1 almost full", () -> doTest(M * M - 1)),
                dynamicTest("height 1 full", () -> doTest(M * M)),
                dynamicTest("height 2 barely filled", () -> doTest(M * M + 1)),
                dynamicTest("height 2 almost full", () -> doTest(M * M * M - 1)),
                dynamicTest("height 2 full", () -> doTest(M * M * M))
        );
    }

    protected void doTest(int size) {
        TrieArray<Integer> instance = new TrieArray<>(size);
        assertEquals(size, instance.size());

        for (int i = 0; i < size; i++) {
            instance.set(i, i + 100);
            assertEquals(instance.get(i), i + 100);
        }
    }
}
