package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.iterator.BreadthFirstSpliterator;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Override
    protected void assertEqualGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        super.assertEqualGraphInt(expected, actual);
        MutableIntAttributed16BitIndexedBidiGraph a = (MutableIntAttributed16BitIndexedBidiGraph) actual;
        for (Integer v : expected.getVertices()) {
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getNextVertices, v), false).collect(Collectors.toList());
                List<Integer> actualBfs = StreamSupport.stream(a.breadthFirstIntSpliterator(v), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getPrevVertices, v), false).collect(Collectors.toList());
                List<Integer> actualBfs = StreamSupport.stream(a.backwardBreadthFirstIntSpliterator(v), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
        }
    }
}
