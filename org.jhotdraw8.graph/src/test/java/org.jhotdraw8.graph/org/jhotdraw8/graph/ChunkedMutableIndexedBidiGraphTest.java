/*
 * @(#)ChunkedMutableIndexedBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.collection.primitive.DenseIntSet8Bit;
import org.jhotdraw8.graph.iterator.BfsDfsVertexSpliterator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ChunkedMutableIndexedBidiGraphTest extends SimpleMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new ChunkedMutableIndexedBidiGraph(4, 1);
    }

    @Override
    public List<DynamicTest> dynamicTestsRandomGraph() {
        return List.of();
    }

    @Override
    @TestFactory
    public List<DynamicTest> dynamicTestsRandomMultiGraph() {
        return Collections.emptyList();
    }

    @Override
    @TestFactory
    public List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Collections.emptyList();
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRemoveAll() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testGraphRemoveAll(8, 0)),
                dynamicTest("arity1", () -> testGraphRemoveAll(8, 1)),
                dynamicTest("arity7", () -> testGraphRemoveAll(8, 7)),
                dynamicTest("arity17", () -> testGraphRemoveAll(20, 17))
        );
    }

    public void testGraphRemoveAll(int vertexCount, int maxArity) throws IOException {
        ChunkedMutableIndexedBidiGraph instance = (ChunkedMutableIndexedBidiGraph) newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphBidiGraphFacade actual = new IndexedBidiGraphBidiGraphFacade(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                if (nextCount < maxArity && prevCount < maxArity) {
                    if (expected.findIndexOfNext(v, u) < 0) {
                        instance.addArrowAsInt(v, u, -u);
                        expected.addArrow(v, u, -u);
                        assertEqualSortedGraphInt(expected, instance);
                    }
                }
            }
        }
        assertEqualSortedGraphInt(expected, instance);

        // Remove random arrows
        for (int i = 0; i < vertexCount; i++) {
            int vidx = rng.nextInt(vertexCount);
            for (int j = expected.getNextCount(vidx) - 1; j >= 0; j--) {
                expected.removeNext(vidx, j);
            }
            instance.removeAllNextAsInt(vidx);
            assertEqualSortedGraphInt(expected, instance);
        }
    }

    @Override
    protected void assertEqualSortedGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        super.assertEqualSortedGraphInt(expected, actual);
        ChunkedMutableIndexedBidiGraph a = (ChunkedMutableIndexedBidiGraph) actual;
        for (Integer v : expected.getVertices()) {
            {
                Set<Integer> expectedBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<Integer>(expected::getNextVertices, v, false), false).collect(Collectors.toCollection(LinkedHashSet::new));
                Set<Integer> actualBfs = StreamSupport.stream(a.searchNextVertexData(v, false, new DenseIntSet8Bit(a.getVertexCount())::addAsInt), false).map(Long::intValue).collect(Collectors.toCollection(LinkedHashSet::new));
                assertEquals(expectedBfs, actualBfs);
            }
            {
                Set<Integer> expectedBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<Integer>(expected::getPrevVertices, v, false), false).collect(Collectors.toCollection(LinkedHashSet::new));
                Set<Integer> actualBfs = StreamSupport.stream(a.searchPrevVertexData(v, false, new DenseIntSet8Bit(a.getVertexCount())::addAsInt), false).map(Long::intValue).collect(Collectors.toCollection(LinkedHashSet::new));
                assertEquals(expectedBfs, actualBfs);
            }
        }
    }
}
