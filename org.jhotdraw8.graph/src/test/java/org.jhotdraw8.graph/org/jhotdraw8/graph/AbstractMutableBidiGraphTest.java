/*
 * @(#)AbstractMutableBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base class for tests that test implementations of the {@link MutableBidiGraph}
 * interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public abstract class AbstractMutableBidiGraphTest<V, A>
        extends AbstractMutableDirectedGraphTest<V, A> {
    @Override
    protected abstract MutableBidiGraph<V, A> newInstance();

    @Override
    protected abstract MutableBidiGraph<V, A> newInstance(DirectedGraph<V, A> g);

    /**
     * Test getters.
     * <pre>
     * <pre>
     *            x
     *           ⟲
     *     0 ─a→ 1 ─c→ 2
     *     │     │
     *     b     d
     *     ↓     ↓
     *     3 ←e─ 4
     * </pre>
     * </pre>
     */
    @Override
    @Test
    public void testAddVerticesAndArrows() {
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        assertEqualsInitialGraph(g);
    }

    @Override
    @Test
    public void testCopy() {
        MutableBidiGraph<V, A> g = newInstance(buildGraph());

        assertEqualsInitialGraph(g);
    }

    private void assertEqualsInitialGraph(MutableBidiGraph<V, A> g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());
        V[] v = getVertices(g);

        assertEquals(0, g.getPrevCount(v[0]));
        assertEquals(2, g.getPrevCount(v[1]));
        assertEquals(1, g.getPrevCount(v[2]));
        assertEquals(2, g.getPrevCount(v[3]));
        assertEquals(1, g.getPrevCount(v[4]));

        assertEquals(v[0], g.getPrev(v[1], 0));
        assertEquals(v[1], g.getPrev(v[1], 1));
        assertEquals(v[1], g.getPrev(v[2], 0));
        assertEquals(v[0], g.getPrev(v[3], 0));
        assertEquals(v[4], g.getPrev(v[3], 1));
        assertEquals(v[1], g.getPrev(v[4], 0));

        assertEquals('a', getArrowId(g.getPrevArrow(v[1], 0)));
        assertEquals('x', getArrowId(g.getPrevArrow(v[1], 1)));
        assertEquals('c', getArrowId(g.getPrevArrow(v[2], 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v[3], 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v[3], 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v[4], 0)));
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
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        V[] v = getVertices(g);

        g.removeArrow(v[0], v[1]);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v[0]));
        assertEquals(1, g.getPrevCount(v[1]));
        assertEquals(1, g.getPrevCount(v[2]));
        assertEquals(2, g.getPrevCount(v[3]));
        assertEquals(1, g.getPrevCount(v[4]));

        assertEquals(v[1], g.getPrev(v[1], 0));
        assertEquals(v[1], g.getPrev(v[2], 0));
        assertEquals(v[0], g.getPrev(v[3], 0));
        assertEquals(v[4], g.getPrev(v[3], 1));
        assertEquals(v[1], g.getPrev(v[4], 0));

        assertEquals('x', getArrowId(g.getPrevArrow(v[1], 0)));
        assertEquals('c', getArrowId(g.getPrevArrow(v[2], 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v[3], 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v[3], 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v[4], 0)));
    }

    /**
     * Test remove arrow.
     * <pre>
     *           ⟲
     *     0 ─a→ 1 ──→ 2
     *     |     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testRemoveArrowAtBidi() {
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        V[] v = getVertices(g);

        assertEquals(v[1], g.getNext(v[0], 0));
        g.removeNext(v[0], 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v[0]));
        assertEquals(1, g.getPrevCount(v[1]));
        assertEquals(1, g.getPrevCount(v[2]));
        assertEquals(2, g.getPrevCount(v[3]));
        assertEquals(1, g.getPrevCount(v[4]));

        assertEquals(v[1], g.getPrev(v[1], 0));
        assertEquals(v[1], g.getPrev(v[2], 0));
        assertEquals(v[0], g.getPrev(v[3], 0));
        assertEquals(v[4], g.getPrev(v[3], 1));
        assertEquals(v[1], g.getPrev(v[4], 0));


        assertEquals('x', getArrowId(g.getPrevArrow(v[1], 0)));
        assertEquals('c', getArrowId(g.getPrevArrow(v[2], 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v[3], 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v[3], 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v[4], 0)));
    }

    /**
     * Test remove arrow with self-loop.
     * <pre>
     *           ⟲x
     *     0 ──→ 1 ──→ 2
     *     |     │
     *     ↓     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testRemoveArrowAtBidiWithSelfLoop() {
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        V[] v = getVertices(g);

        assertEquals(v[1], g.getNext(v[1], 0));
        g.removeNext(v[1], 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v[0]));
        assertEquals(1, g.getPrevCount(v[1]));
        assertEquals(1, g.getPrevCount(v[2]));
        assertEquals(2, g.getPrevCount(v[3]));
        assertEquals(1, g.getPrevCount(v[4]));

        assertEquals(v[0], g.getPrev(v[1], 0));
        assertEquals(v[1], g.getPrev(v[2], 0));
        assertEquals(v[0], g.getPrev(v[3], 0));
        assertEquals(v[4], g.getPrev(v[3], 1));
        assertEquals(v[1], g.getPrev(v[4], 0));


        assertEquals('a', getArrowId(g.getPrevArrow(v[1], 0)));
        assertEquals('c', getArrowId(g.getPrevArrow(v[2], 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v[3], 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v[3], 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v[4], 0)));
    }

    /**
     * Test remove vertex 1.
     * <p>
     * Example graph after removal of 1:
     * <pre>
     *     0          2
     *     │
     *     ↓
     *     3 ←── 4
     * </pre>
     */
    @Test
    public void testRemoveVertexBidi() {
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        V[] v = getVertices(g);

        g.removeVertex(v[1]);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v[0]));
        assertEquals(0, g.getPrevCount(v[2]));
        assertEquals(2, g.getPrevCount(v[3]));
        assertEquals(0, g.getPrevCount(v[4]));

        assertEquals(v[0], g.getPrev(v[3], 0));
        assertEquals(v[4], g.getPrev(v[3], 1));

        assertEquals('b', getArrowId(g.getPrevArrow(v[3], 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v[3], 1)));
    }
}