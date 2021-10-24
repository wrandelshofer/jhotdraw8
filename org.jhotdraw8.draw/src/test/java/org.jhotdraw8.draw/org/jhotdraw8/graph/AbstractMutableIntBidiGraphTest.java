package org.jhotdraw8.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractMutableIntBidiGraphTest
        extends AbstractMutableIntDirectedGraphTest {
    protected abstract MutableIntBidiGraph newInstance();

    protected abstract MutableIntBidiGraph newInstance(int vertexCount);

    protected abstract MutableIntBidiGraph newInstance(MutableIntBidiGraph g);

    /**
     * Test getters.
     * <pre>
     *     0 ──→ 1 ──→ 2
     *     │     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testAddVerticesAndArrowsBidi() {
        MutableIntBidiGraph g = (MutableIntBidiGraph) buildGraph2();

        assertEqualsInitialGraph(g);
    }

    @Test
    public void testCopy() {
        MutableIntBidiGraph g = newInstance((MutableIntBidiGraph) buildGraph2());

        assertEqualsInitialGraph(g);
    }

    private void assertEqualsInitialGraph(MutableIntBidiGraph g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());

        assertEquals(0, g.getPrevCount(0));
        assertEquals(2, g.getPrevCount(1));
        assertEquals(1, g.getPrevCount(2));
        assertEquals(2, g.getPrevCount(3));
        assertEquals(1, g.getPrevCount(4));

        assertEquals(0, g.getPrev(1, 0));
        assertEquals(1, g.getPrev(1, 1));
        assertEquals(1, g.getPrev(2, 0));
        assertEquals(0, g.getPrev(3, 0));
        assertEquals(4, g.getPrev(3, 1));
        assertEquals(1, g.getPrev(4, 0));
    }

    /**
     * Test arrow removal.
     * <pre>
     *     0 ─x→ 1 ──→ 2
     *     |     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testRemoveArrowBidi() {
        MutableIntBidiGraph g = (MutableIntBidiGraph) buildGraph2();

        g.removeArrow(0, 1);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(0));
        assertEquals(1, g.getPrevCount(1));
        assertEquals(1, g.getPrevCount(2));
        assertEquals(2, g.getPrevCount(3));
        assertEquals(1, g.getPrevCount(4));

        assertEquals(1, g.getPrev(1, 0));
        assertEquals(1, g.getPrev(2, 0));
        assertEquals(0, g.getPrev(3, 0));
        assertEquals(4, g.getPrev(3, 1));
        assertEquals(1, g.getPrev(4, 0));
    }

    /**
     * Test arrowAt removal.
     * <pre>
     *           ⟲
     *     0 ─x→ 1 ──→ 2
     *     |     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testRemoveArrowAtBidi() {
        MutableIntBidiGraph g = (MutableIntBidiGraph) buildGraph2();

        g.removeArrowAt(0, 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(0));
        assertEquals(1, g.getPrevCount(1));
        assertEquals(1, g.getPrevCount(2));
        assertEquals(2, g.getPrevCount(3));
        assertEquals(1, g.getPrevCount(4));

        assertEquals(1, g.getPrev(1, 0));
        assertEquals(1, g.getPrev(2, 0));
        assertEquals(0, g.getPrev(3, 0));
        assertEquals(4, g.getPrev(3, 1));
        assertEquals(1, g.getPrev(4, 0));
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
    public void testRemoveVertexBidi() {
        MutableIntBidiGraph g = (MutableIntBidiGraph) buildGraph2();

        g.removeVertex(1);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(0, g.getPrevCount(0));
        assertEquals(0, g.getPrevCount(1));
        assertEquals(2, g.getPrevCount(2));
        assertEquals(0, g.getPrevCount(3));

        assertEquals(0, g.getPrev(2, 0));
        assertEquals(3, g.getPrev(2, 1));
    }


}