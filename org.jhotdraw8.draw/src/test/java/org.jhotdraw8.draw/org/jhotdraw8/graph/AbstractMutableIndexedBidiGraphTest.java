package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractMutableIndexedBidiGraphTest {
    protected abstract MutableIndexedBidiGraph newInstance(int maxArity);

    private void assertEqualGraphs(BidiGraph<Integer, Integer> expected, BidiGraph<Integer, Integer> actual) throws IOException {
        StringWriter expectedW = new StringWriter();
        DumpGraph.dumpAsDot(expectedW, expected);
        StringWriter actualW = new StringWriter();
        DumpGraph.dumpAsDot(actualW, actual);
        assertEquals(expectedW.toString(), actualW.toString());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraph() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testCreateRandomGraph(8, 0)),
                dynamicTest("arity1", () -> testCreateRandomGraph(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraph(8, 7))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Arrays.asList(
                dynamicTest("arity1", () -> testCreateRandomGraphWithArrowData(8, 1)),
                dynamicTest("arity7", () -> testCreateRandomGraphWithArrowData(8, 7))
        );
    }


    public void testCreateRandomGraph(int vertexCount, int maxArity) throws IOException {
        MutableIndexedBidiGraph instance = newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertex(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);
                assertEquals(expected.findIndexOfNext(v, u) < 0, instance.findIndexOfNextAsInt(v, u) < 0);
                assertEquals(expected.findIndexOfPrev(u, v) < 0, instance.findIndexOfPrevAsInt(u, v) < 0);

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                assertEquals(nextCount, instance.getNextCount(v));
                assertEquals(prevCount, instance.getPrevCount(u));

                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrow(v, u, -u);
                    expected.addArrow(v, u, -u);
                }

                assertEquals(expected.getNextCount(v), instance.getNextCount(v));
                assertEquals(expected.getNextVertices(v), actual.getNextVertices(v));
                assertEquals(expected.getPrevCount(u), instance.getPrevCount(u));
                assertEquals(expected.getPrevVertices(v), actual.getPrevVertices(v));
                assertEquals(expected.findIndexOfNext(v, u) < 0,
                        actual.findIndexOfNext(v, u) < 0);
                assertEquals(expected.findIndexOfPrev(u, v) < 0,
                        actual.findIndexOfPrev(u, v) < 0, "expected: " + expected.findIndexOfPrev(u, v) + ", actual: " + actual.findIndexOfPrev(u, v));
                assertEquals(expected.findIndexOfPrev(u, v),
                        actual.findIndexOfPrev(u, v));
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
                    instance.removeArrow(vidx, uidx);
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
            instance.addVertex(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int vidx = 0; vidx < vertexCount; vidx++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int uidx = rng.nextInt(vertexCount);
                assertEquals(expected.findIndexOfNext(vidx, uidx) < 0, instance.findIndexOfNextAsInt(vidx, uidx) < 0);
                assertEquals(expected.findIndexOfPrev(uidx, vidx) < 0, instance.findIndexOfPrevAsInt(uidx, vidx) < 0);

                int nextCount = expected.getNextCount(vidx);
                int prevCount = expected.getPrevCount(uidx);
                assertEquals(nextCount, instance.getNextCount(vidx));
                assertEquals(prevCount, instance.getPrevCount(uidx));

                if (nextCount < maxArity && prevCount < maxArity) {
                    instance.addArrow(vidx, uidx, vidx * 100 + uidx);
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
                    instance.removeArrow(vidx, uidx);
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
}
