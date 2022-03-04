package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collections;
import java.util.List;

public class ChunkedMutableIntAttributed32BitIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new ChunkedMutableIntAttributed32BitIndexedBidiGraph();
    }

    @Override
    public @NonNull List<DynamicTest> dynamicTestsRandomGraph() {
        return List.of();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomMultiGraph() {
        return Collections.emptyList();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Collections.emptyList();
    }
}
