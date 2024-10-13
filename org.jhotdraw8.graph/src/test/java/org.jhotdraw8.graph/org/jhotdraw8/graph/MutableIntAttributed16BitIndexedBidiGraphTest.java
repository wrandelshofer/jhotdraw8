/*
 * @(#)MutableIntAttributed16BitIndexedBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.graph.iterator.BfsDfsVertexSpliterator;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableIntAttributed16BitIndexedBidiGraphTest extends SimpleMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new MutableIntAttributed16BitIndexedBidiGraph(0, maxArity);
    }

    @Override
    public List<DynamicTest> dynamicTestsRandomSortedGraph() {
        return Collections.emptyList();
    }

    @Override
    public List<DynamicTest> dynamicTestsRandomSortedGraphWithArrowData() {
        return Collections.emptyList();
    }

    @Override
    protected void assertEqualGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        super.assertEqualGraphInt(expected, actual);
        MutableIntAttributed16BitIndexedBidiGraph a = (MutableIntAttributed16BitIndexedBidiGraph) actual;
        for (Integer v : expected.getVertices()) {
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<Integer>(expected::getNextVertices, v, false), false).collect(Collectors.toList());
                List<Integer> actualBfs = StreamSupport.stream(a.seachNextVerticesAsInt(v, false), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
            {
                List<Integer> expectedBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<Integer>(expected::getPrevVertices, v, false), false).collect(Collectors.toList());
                List<Integer> actualBfs = StreamSupport.stream(a.searchPrevVerticesAsInt(v, false), false).collect(Collectors.toList());
                assertEquals(expectedBfs, actualBfs);
            }
        }
    }
}
