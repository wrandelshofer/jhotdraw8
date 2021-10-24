package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractMutableIntDirectedGraphTest {
    protected abstract MutableIntDirectedGraph newInstance();

    protected abstract MutableIntDirectedGraph newInstance(int vertexCount);

    protected abstract MutableIntDirectedGraph newInstance(MutableIntDirectedGraph g);

    /**
     * Example graph:
     * <pre>
     *           ⟲
     *     0 ──→ 1 ──→ 2
     *     │     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @NonNull
    protected MutableIntDirectedGraph buildGraph2() {
        MutableIntDirectedGraph g = newInstance(5);
        g.addArrow(0, 1);
        g.addArrow(0, 3);
        g.addArrow(1, 1);
        g.addArrow(1, 2);
        g.addArrow(1, 4);
        g.addArrow(4, 3);
        return g;
    }

    @NonNull
    protected MutableIntDirectedGraph buildGraph1() {
        MutableIntDirectedGraph g = newInstance();
        g.addVertex();
        g.addVertex();
        g.addVertex();
        g.addVertex();
        g.addVertex();
        g.addArrow(0, 1);
        g.addArrow(0, 3);
        g.addArrow(1, 1);
        g.addArrow(1, 2);
        g.addArrow(1, 4);
        g.addArrow(4, 3);
        return g;
    }

    /**
     * Test getters.
     */
    @Test
    public void testAddVerticesAndArrows() {
        MutableIntDirectedGraph g = buildGraph1();

        assertEqualsGraph1(g);
    }

    private void assertEqualsGraph1(MutableIntDirectedGraph g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());
        assertEquals(2, g.getNextCount(0));
        assertEquals(3, g.getNextCount(1));
        assertEquals(0, g.getNextCount(2));
        assertEquals(0, g.getNextCount(3));
        assertEquals(1, g.getNextCount(4));

        assertEquals(1, g.getNext(0, 0));
        assertEquals(3, g.getNext(0, 1));
        assertEquals(1, g.getNext(1, 0));
        assertEquals(2, g.getNext(1, 1));
        assertEquals(4, g.getNext(1, 2));
        assertEquals(3, g.getNext(4, 0));
    }

    /**
     * Test getters.
     */
    @Test
    public void testCopy() {
        MutableIntDirectedGraph g = newInstance(buildGraph2());

        assertEqualsGraph1(g);
    }

    /**
     * Test arrow removal.
     */
    @Test
    public void testRemoveArrow() {
        MutableIntDirectedGraph g = buildGraph2();

        g.removeArrow(0, 1);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(1, g.getNextCount(0));
        assertEquals(3, g.getNextCount(1));
        assertEquals(0, g.getNextCount(2));
        assertEquals(0, g.getNextCount(3));
        assertEquals(1, g.getNextCount(4));

        assertEquals(3, g.getNext(0, 0));
        assertEquals(1, g.getNext(1, 0));
        assertEquals(2, g.getNext(1, 1));
        assertEquals(4, g.getNext(1, 2));
        assertEquals(3, g.getNext(4, 0));
    }

    /**
     * Test arrowAt removal.
     */
    @Test
    public void testRemoveArrowAt() {
        MutableIntDirectedGraph g = buildGraph2();

        g.removeArrowAt(0, 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(1, g.getNextCount(0));
        assertEquals(3, g.getNextCount(1));
        assertEquals(0, g.getNextCount(2));
        assertEquals(0, g.getNextCount(3));
        assertEquals(1, g.getNextCount(4));

        assertEquals(3, g.getNext(0, 0));
        assertEquals(1, g.getNext(1, 0));
        assertEquals(2, g.getNext(1, 1));
        assertEquals(4, g.getNext(1, 2));
        assertEquals(3, g.getNext(4, 0));
    }

    /**
     * Test remove vertex 1.
     * <p>
     * Example graph after removal of 1:
     * <pre>
     *     0          1
     *     │
     *     ↓
     *     2 ←── 3
     * </pre>
     */
    @Test
    public void testRemoveVertex() {
        MutableIntDirectedGraph g = buildGraph2();

        g.removeVertex(1);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(1, g.getNextCount(0));
        assertEquals(0, g.getNextCount(1));
        assertEquals(0, g.getNextCount(2));
        assertEquals(1, g.getNextCount(3));

        assertEquals(2, g.getNext(0, 0));
        assertEquals(2, g.getNext(3, 0));
    }


}