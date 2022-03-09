package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractMutableIndexedBidiGraphTest {
    protected abstract MutableIndexedBidiGraph newInstance(int maxArity);

    private void assertEqualGraphs(BidiGraph<Integer, Integer> expected, BidiGraph<Integer, Integer> actual) throws IOException {
        StringWriter expectedW = new StringWriter();
        DumpGraph.dumpAsDot(expectedW, expected);
        StringWriter actualW = new StringWriter();
        DumpGraph.dumpAsDot(actualW, actual);
        String[] expectedSplit = expectedW.toString().split("\n");
        String[] actualSplit = actualW.toString().split("\n");
        Arrays.sort(expectedSplit);
        Arrays.sort(actualSplit);
        assertEquals(Arrays.asList(actualSplit), Arrays.asList(actualSplit));
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomGraph(20, 17))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomSortedGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomSortedGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomSortedGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomSortedGraph(20, 17))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomMultiGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomMultiGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomMultiGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomMultiGraph(8, 7)),
                dynamicTest("arity17", () -> testCreateRandomMultiGraph(20, 17))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Arrays.asList(
                dynamicTest("arity1", () -> testCreateRandomGraphWithArrowData(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraphWithArrowData(8, 7))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomSortedGraphWithArrowData() {
        return Arrays.asList(
                dynamicTest("arity1", () -> testCreateRandomSortedGraphWithArrowData(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomSortedGraphWithArrowData(8, 7))
        );
    }


    public void testCreateRandomGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);
                assertEquals(expected.findIndexOfNext(v, u), instance.findIndexOfNextAsInt(v, u));
                assertEquals(expected.findIndexOfPrev(u, v), instance.findIndexOfPrevAsInt(u, v));

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                assertEquals(nextCount, instance.getNextCount(v));
                assertEquals(prevCount, instance.getPrevCount(u));

                if (nextCount < maxArity && prevCount < maxArity) {
                    if (expected.findIndexOfNext(v, u) < 0) {
                        instance.addArrowAsInt(v, u, -u);
                        expected.addArrow(v, u, -u);
                    }
                }
                assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
                assertEquals(expected.findIndexOfNext(v, u),
                        actual.findIndexOfNext(v, u));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v), "expected: " + expected.findIndexOfPrev(u, v) + ", actual: " + actual.findIndexOfPrev(u, v));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v));
            }
        }

        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());

        assertEqualGraphs(expected, actual);

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                }

                assertEquals(expected.getNextCount(vidx), instance.getNextCount(vidx));
                assertEquals(expected.getNextVertices(vidx), actual.getNextVertices(vidx));
                assertEquals(expected.getPrevCount(uidx), instance.getPrevCount(uidx));
                assertEquals(expected.getPrevVertices(vidx), actual.getPrevVertices(vidx));
            }
        }
        assertEqualGraphs(expected, actual);
    }

    public void testCreateRandomSortedGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
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
                    }
                }
                assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
                assertEquals(findSortedIndex(expected.getNextVertices(v), u),
                        actual.findIndexOfNext(v, u));
                assertEquals(findSortedIndex(expected.getPrevVertices(u), v),
                        actual.findIndexOfPrev(u, v));
            }
        }

        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());

        assertEqualGraphs(expected, actual);

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                }

                assertEquals(expected.getNextCount(vidx), instance.getNextCount(vidx));
                assertEquals(expected.getNextVertices(vidx), actual.getNextVertices(vidx));
                assertEquals(expected.getPrevCount(uidx), instance.getPrevCount(uidx));
                assertEquals(expected.getPrevVertices(vidx), actual.getPrevVertices(vidx));
            }
        }
        assertEqualGraphs(expected, actual);
    }

    private int findSortedIndex(Collection<Integer> vertices, int v) {
        final Integer[] a = vertices.toArray(new Integer[0]);
        Arrays.sort(a);
        return Arrays.binarySearch(a, v);
    }

    public void testCreateRandomMultiGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);
                assertEquals(expected.findIndexOfNext(v, u), instance.findIndexOfNextAsInt(v, u));
                assertEquals(expected.findIndexOfPrev(u, v), instance.findIndexOfPrevAsInt(u, v));

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                assertEquals(nextCount, instance.getNextCount(v));
                assertEquals(prevCount, instance.getPrevCount(u));

                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrowAsInt(v, u, -u);
                    expected.addArrow(v, u, -u);
                }
                assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
                assertEquals(expected.findIndexOfNext(v, u),
                        actual.findIndexOfNext(v, u));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v), "expected: " + expected.findIndexOfPrev(u, v) + ", actual: " + actual.findIndexOfPrev(u, v));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v));
            }
        }

        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());

        assertEqualGraphs(expected, actual);

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                }

                assertEquals(expected.getNextCount(vidx), instance.getNextCount(vidx));
                assertEquals(expected.getNextVertices(vidx), actual.getNextVertices(vidx));
                assertEquals(expected.getPrevCount(uidx), instance.getPrevCount(uidx));
                assertEquals(expected.getPrevVertices(vidx), actual.getPrevVertices(vidx));
            }
        }
        assertEqualGraphs(expected, actual);
    }

    public void testCreateRandomGraphWithArrowData(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int uidx = rng.nextInt(vertexCount);
                assertEquals(expected.findIndexOfNext(vidx, uidx), instance.findIndexOfNextAsInt(vidx, uidx));
                assertEquals(expected.findIndexOfPrev(uidx, vidx), instance.findIndexOfPrevAsInt(uidx, vidx));

                int nextCount = expected.getNextCount(vidx);
                int prevCount = expected.getPrevCount(uidx);
                assertEquals(nextCount, instance.getNextCount(vidx));
                assertEquals(prevCount, instance.getPrevCount(uidx));

                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrowAsInt(vidx, uidx, vidx * 100 + uidx);
                    expected.addArrow(vidx, uidx, vidx * 100 + uidx);


                    assertEquals(expected.getNextCount(vidx), instance.getNextCount(vidx));
                    assertEquals(expected.getNextVertices(vidx), actual.getNextVertices(vidx));
                    assertEquals(expected.getPrevCount(uidx), instance.getPrevCount(uidx));
                    assertEquals(expected.getPrevVertices(vidx), actual.getPrevVertices(vidx));
                    assertEquals(expected.findIndexOfNext(vidx, uidx),
                            actual.findIndexOfNext(vidx, uidx));
                    assertEquals(expected.findIndexOfPrev(uidx, vidx),
                            actual.findIndexOfPrev(uidx, vidx));
                    assertEquals(expected.getNextArrow(vidx, expected.findIndexOfNext(vidx, uidx)),
                            actual.getNextArrow(vidx, actual.findIndexOfNext(vidx, uidx)));
                    assertEquals(expected.getPrevArrow(uidx, expected.findIndexOfPrev(uidx, vidx)),
                            actual.getPrevArrow(uidx, actual.findIndexOfPrev(uidx, vidx)));
                }
            }
        }

        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());
        assertEquals(expected.getArrows(), actual.getArrows());

        assertEqualGraphs(expected, actual);

        // Remove random arrows
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int nextCount = expected.getNextCount(vidx);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int uidx = expected.getNext(vidx, i);
                assertEquals(uidx, instance.getNextAsInt(vidx, i));
                assertEquals(expected.findIndexOfNext(vidx, uidx), instance.findIndexOfNextAsInt(vidx, uidx));
                if (expected.findIndexOfNext(vidx, uidx) != -1) {
                    instance.removeArrowAsInt(vidx, uidx);
                    expected.removeArrow(vidx, uidx);
                }

                assertEquals(expected.getNextCount(vidx), instance.getNextCount(vidx));
                assertEquals(expected.getNextVertices(vidx), actual.getNextVertices(vidx));
                assertEquals(expected.getPrevCount(uidx), instance.getPrevCount(uidx));
                assertEquals(expected.getPrevVertices(vidx), actual.getPrevVertices(vidx));
            }
        }
        assertEqualGraphs(expected, actual);
    }

    public void testCreateRandomSortedGraphWithArrowData(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
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

                if (nextCount < maxArity && prevCount < maxArity && expected.findIndexOfNext(v, u) < 0) {
                    instance.addArrowAsInt(v, u, v * 100 + u);
                    expected.addArrow(v, u, v * 100 + u);


                    assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                    assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                    assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                    assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
                    assertEquals(findSortedIndex(expected.getNextVertices(v), u),
                            actual.findIndexOfNext(v, u));
                    assertEquals(findSortedIndex(expected.getPrevVertices(u), v),
                            actual.findIndexOfPrev(u, v));
                    assertEquals(expected.getNextArrow(v, expected.findIndexOfNext(v, u)),
                            actual.getNextArrow(v, actual.findIndexOfNext(v, u)));
                    assertEquals(expected.getPrevArrow(u, expected.findIndexOfPrev(u, v)),
                            actual.getPrevArrow(u, actual.findIndexOfPrev(u, v)));
                }
            }
        }

        assertEquals(expected.getVertexCount(), actual.getVertexCount());
        assertEquals(expected.getVertices(), actual.getVertices());
        assertEquals(expected.getArrowCount(), actual.getArrowCount());
        assertEquals(new LinkedHashSet<>(expected.getArrows()), new LinkedHashSet<>(actual.getArrows()));

        assertEqualGraphs(expected, actual);

        // Remove random arrows
        for (int v = 0; v < vertexCount; v++) {
            int nextCount = expected.getNextCount(v);
            if (nextCount > 0) {
                int i = rng.nextInt(nextCount);
                int u = expected.getNext(v, i);
                if (expected.findIndexOfNext(v, u) != -1) {
                    instance.removeArrowAsInt(v, u);
                    expected.removeArrow(v, u);
                }

                assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
            }
        }
        assertEqualGraphs(expected, actual);
    }
}
