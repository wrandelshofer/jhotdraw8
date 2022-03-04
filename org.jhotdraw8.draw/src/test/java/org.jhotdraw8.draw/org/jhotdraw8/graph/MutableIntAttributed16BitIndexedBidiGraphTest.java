package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collections;
import java.util.List;

public class MutableIntAttributed16BitIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new MutableIntAttributed16BitIndexedBidiGraph(0, maxArity);
    }

    @Override
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraph() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraphWithArrowData() {
        return Collections.emptyList();
    }


}
