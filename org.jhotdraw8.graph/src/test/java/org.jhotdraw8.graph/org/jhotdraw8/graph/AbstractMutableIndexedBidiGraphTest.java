/*
 * @(#)AbstractMutableIndexedBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.graph.io.GraphvizWriter;
import org.jhotdraw8.graph.iterator.BfsDfsVertexSpliterator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractMutableIndexedBidiGraphTest {
    protected abstract MutableIndexedBidiGraph newInstance(int maxArity);

    protected void assertEqualGraphInt(BidiGraph<Integer, Integer> expected,
                                       IndexedBidiGraph actual) {
        for (Integer v : expected.getVertices()) {
            List<Integer> actualNextList =
                    StreamSupport.stream(actual.nextVerticesEnumerator(v), false).collect(Collectors.toList());
            assertEquals(new ArrayList<>(expected.getNextVertices(v)), actualNextList);

            List<Integer> actualPrevList =
                    StreamSupport.stream(actual.prevVerticesEnumerator(v), false).collect(Collectors.toList());
            assertEquals(new ArrayList<>(expected.getPrevVertices(v)), actualPrevList);
        }
    }

    protected void assertEqualSortedGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        for (Integer v : expected.getVertices()) {
            Set<Integer> actualNextList =
                    StreamSupport.stream(actual.nextVerticesEnumerator(v), false).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(new LinkedHashSet<>(expected.getNextVertices(v)), actualNextList);

            Set<Integer> actualPrevList =
                    StreamSupport.stream(actual.prevVerticesEnumerator(v), false).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(new LinkedHashSet<>(expected.getPrevVertices(v)), actualPrevList);
        }
    }

    protected void assertEqualGraph(BidiGraph<Integer, Integer> expected, BidiGraph<Integer, Integer> actual) throws IOException {
        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());


        StringWriter expectedW = new StringWriter();
        new GraphvizWriter().write(expectedW, expected);
        StringWriter actualW = new StringWriter();
        new GraphvizWriter().write(actualW, actual);
        String[] expectedSplit = expectedW.toString().split("\n");
        String[] actualSplit = actualW.toString().split("\n");
        Arrays.sort(expectedSplit);
        Arrays.sort(actualSplit);
        assertEquals(Arrays.asList(actualSplit), Arrays.asList(actualSplit));

        for (Integer v : expected.getVertices()) {
            assertEquals(expected.getNextCount(v), actual.getNextCount(v));
            assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
            assertEquals(expected.getPrevCount(v), actual.getPrevCount(v));
            assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
            for (Integer u : expected.getVertices()) {
                assertEquals(expected.findIndexOfNext(v, u),
                        actual.findIndexOfNext(v, u));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v));
            }
            List<Integer> expectedBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<>(expected::getNextVertices, v, false), false).collect(Collectors.toList());
            List<Integer> actualBfs = StreamSupport.stream(new BfsDfsVertexSpliterator<>(actual::getNextVertices, v, false), false).collect(Collectors.toList());
            assertEquals(expectedBfs, actualBfs);
        }
    }

    private void assertEqualSortedGraph(BidiGraph<Integer, Integer> expected,
                                        BidiGraph<Integer, Integer> actual) throws IOException {
        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());

        StringWriter expectedW = new StringWriter();
        new GraphvizWriter().write(expectedW, expected);
        StringWriter actualW = new StringWriter();
        new GraphvizWriter().write(actualW, actual);
        String[] expectedSplit = expectedW.toString().split("\n");
        String[] actualSplit = actualW.toString().split("\n");
        Arrays.sort(expectedSplit);
        Arrays.sort(actualSplit);
        assertEquals(Arrays.asList(actualSplit), Arrays.asList(actualSplit));

        for (Integer v : expected.getVertices()) {
            assertEquals(expected.getNextCount(v), actual.getNextCount(v));
            assertEquals(new LinkedHashSet<>(expected.getNextVertices(v)),
                    new LinkedHashSet<>(actual.getNextVertices(v)));
            assertEquals(expected.getPrevCount(v), actual.getPrevCount(v));
            assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
            for (Integer u : expected.getVertices()) {
                assertEquals(findSortedIndex(expected.getNextVertices(v), u),
                        actual.findIndexOfNext(v, u));
                assertEquals(findSortedIndex(expected.getPrevVertices(u), v),
                        actual.findIndexOfPrev(u, v));
            }
        }
        if (actual instanceof IndexedBidiGraph) {
            assertEqualGraphInt(expected, (IndexedBidiGraph) actual);
        }
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRandomGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomGraph(20, 17))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRandomSortedGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomSortedGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomSortedGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomSortedGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomSortedGraph(20, 17))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRandomMultiGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomMultiGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomMultiGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomMultiGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomMultiGraph(20, 17))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Arrays.asList(
                dynamicTest("arity1", () -> testCreateRandomGraphWithArrowData(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraphWithArrowData(8, 7))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsRandomSortedGraphWithArrowData() {
        return Arrays.asList(
                dynamicTest("arity1", () -> testCreateRandomSortedGraphWithArrowData(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomSortedGraphWithArrowData(8, 7))
        );
    }


    public void testCreateRandomGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
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
                        assertEqualGraph(expected, actual);
                        assertEqualGraphInt(expected, instance);
                    }
                }
            }
        }
        assertEqualGraph(expected, actual);
        assertEqualGraphInt(expected, instance);

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);

                    assertEqualGraph(expected, actual);
                    assertEqualGraphInt(expected, instance);
                }
            }
        }
        assertEqualGraph(expected, actual);
        assertEqualGraphInt(expected, instance);
    }

    public void testCreateRandomSortedGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphBidiGraphFacade actual = new IndexedBidiGraphBidiGraphFacade(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
            assertEqualSortedGraph(expected, actual);
            assertEqualSortedGraphInt(expected, instance);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);
                assertEquals(findSortedIndex(expected.getNextVertices(v), u), instance.findIndexOfNextAsInt(v, u));
                assertEquals(findSortedIndex(expected.getPrevVertices(u), v), instance.findIndexOfPrevAsInt(u, v));

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                assertEquals(nextCount, instance.getNextCount(v));
                assertEquals(prevCount, instance.getPrevCount(u));

                if (nextCount < maxArity && prevCount < maxArity) {
                    if (expected.findIndexOfNext(v, u) < 0) {
                        instance.addArrowAsInt(v, u, -u);
                        expected.addArrow(v, u, -u);
                        assertEqualSortedGraph(expected, actual);
                        assertEqualSortedGraphInt(expected, instance);
                    }
                }
            }
        }


        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                    assertEqualSortedGraph(expected, actual);
                    assertEqualSortedGraphInt(expected, instance);
                }
            }
        }
    }

    private int findSortedIndex(Collection<Integer> vertices, int v) {
        final Integer[] a = vertices.toArray(new Integer[0]);
        Arrays.sort(a);
        return Arrays.binarySearch(a, v);
    }

    public void testCreateRandomMultiGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphBidiGraphFacade actual = new IndexedBidiGraphBidiGraphFacade(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
            assertEqualGraph(expected, actual);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);
                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrowAsInt(v, u, -u);
                    expected.addArrow(v, u, -u);
                    assertEqualGraph(expected, actual);
                }
            }
        }


        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                    assertEqualGraph(expected, actual);
                }
            }
        }
        assertEqualGraph(expected, actual);
    }

    public void testCreateRandomGraphWithArrowData(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphBidiGraphFacade actual = new IndexedBidiGraphBidiGraphFacade(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
            assertEqualGraph(expected, actual);
        }

        Random rng = new Random(0);
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int uidx = rng.nextInt(vertexCount);

                int nextCount = expected.getNextCount(vidx);
                int prevCount = expected.getPrevCount(uidx);

                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrowAsInt(vidx, uidx, vidx * 100 + uidx);
                    expected.addArrow(vidx, uidx, vidx * 100 + uidx);
                    assertEqualGraph(expected, actual);
                }
            }
        }

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                    assertEqualGraph(expected, actual);
                }
            }
        }
    }

    public void testCreateRandomSortedGraphWithArrowData(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphBidiGraphFacade actual = new IndexedBidiGraphBidiGraphFacade(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
            assertEqualSortedGraph(expected, actual);
            assertEqualSortedGraphInt(expected, instance);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);

                if (nextCount < maxArity && prevCount < maxArity && expected.findIndexOfNext(v, u) < 0) {
                    instance.addArrowAsInt(v, u, v * 100 + u);
                    expected.addArrow(v, u, v * 100 + u);

                    assertEqualSortedGraph(expected, actual);
                    assertEqualSortedGraphInt(expected, instance);
                }
            }
        }


        // Remove random arrows
        for (int v = 0; v < vertexCount; v++) {
            int nextCount = expected.getNextCount(v);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int u = expected.getNext(v, i);
                if (expected.findIndexOfNext(v, u) >= 0) {
                    instance.removeArrowAsInt(v, u);
                    expected.removeArrow(v, u);
                    assertEqualSortedGraph(expected, actual);
                    assertEqualSortedGraphInt(expected, instance);
                }
            }
        }
    }
}
