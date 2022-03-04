package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collections;
import java.util.List;

public class SimpleMutableIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new SimpleMutableIndexedBidiGraph(0, maxArity);
    }

    @Override
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraph() {
        return Collections.emptyList();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Collections.emptyList();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraphWithArrowData() {
        return Collections.emptyList();
    }
}
