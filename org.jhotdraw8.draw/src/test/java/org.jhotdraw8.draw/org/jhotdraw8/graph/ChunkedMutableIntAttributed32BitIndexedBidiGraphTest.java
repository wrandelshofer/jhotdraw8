package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.iterator.BreadthFirstSpliterator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Override
    protected void assertEqualGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        super.assertEqualGraphInt(expected, actual);
        ChunkedMutableIntAttributed32BitIndexedBidiGraph a = (ChunkedMutableIntAttributed32BitIndexedBidiGraph) actual;
        for (Integer v : expected.getVertices()) {
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getNextVertices, v), false).collect(Collectors.toList());
                List<Integer> actualBfs = new ArrayList<>();//StreamSupport.stream(a.breadthFirstIntSpliterator(v), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getPrevVertices, v), false).collect(Collectors.toList());
                List<Integer> actualBfs = new ArrayList<>();// StreamSupport.stream(a.backwardBreadthFirstIntSpliterator(v), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
        }
    }
}
