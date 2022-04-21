/*
 * @(#)AbstractMutableBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractMutableBidiGraphTest<V, A>
        extends AbstractMutableDirectedGraphTest<V, A> {
    protected abstract MutableBidiGraph<V, A> newInstance();

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
    @Test
    public void testAddVerticesAndArrowsBidi() {
        MutableBidiGraph<V, A> g = (MutableBidiGraph<V, A>) buildGraph();
        assertEqualsInitialGraph(g);
    }

    @Test
    public void testCopy() {
        MutableBidiGraph<V, A> g = newInstance(buildGraph());

        assertEqualsInitialGraph(g);
    }

    private void assertEqualsInitialGraph(MutableBidiGraph<V, A> g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);

        assertEquals(0, g.getPrevCount(v0));
        assertEquals(2, g.getPrevCount(v1));
        assertEquals(1, g.getPrevCount(v2));
        assertEquals(2, g.getPrevCount(v3));
        assertEquals(1, g.getPrevCount(v4));

        assertEquals(v0, g.getPrev(v1, 0));
        assertEquals(v1, g.getPrev(v1, 1));
        assertEquals(v1, g.getPrev(v2, 0));
        assertEquals(v0, g.getPrev(v3, 0));
        assertEquals(v4, g.getPrev(v3, 1));
        assertEquals(v1, g.getPrev(v4, 0));

        assertEquals('a', getArrowId(g.getPrevArrow(v1, 0)));
        assertEquals('x', getArrowId(g.getPrevArrow(v1, 1)));
        assertEquals('c', getArrowId(g.getPrevArrow(v2, 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v3, 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v3, 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v4, 0)));
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
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);

        g.removeArrow(v0, v1);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v0));
        assertEquals(1, g.getPrevCount(v1));
        assertEquals(1, g.getPrevCount(v2));
        assertEquals(2, g.getPrevCount(v3));
        assertEquals(1, g.getPrevCount(v4));

        assertEquals(1, g.getPrev(v1, 0));
        assertEquals(1, g.getPrev(v2, 0));
        assertEquals(0, g.getPrev(v3, 0));
        assertEquals(4, g.getPrev(v3, 1));
        assertEquals(1, g.getPrev(v4, 0));

        assertEquals('x', g.getPrevArrow(v1, 0));
        assertEquals('c', g.getPrevArrow(v2, 0));
        assertEquals('b', g.getPrevArrow(v3, 0));
        assertEquals('e', g.getPrevArrow(v3, 1));
        assertEquals('d', g.getPrevArrow(v4, 0));
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
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);

        assertEquals(v1, g.getNext(v0, 0));
        g.removeNext(v0, 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v0));
        assertEquals(1, g.getPrevCount(v1));
        assertEquals(1, g.getPrevCount(v2));
        assertEquals(2, g.getPrevCount(v3));
        assertEquals(1, g.getPrevCount(v4));

        assertEquals(v1, g.getPrev(v1, 0));
        assertEquals(v1, g.getPrev(v2, 0));
        assertEquals(v0, g.getPrev(v3, 0));
        assertEquals(v4, g.getPrev(v3, 1));
        assertEquals(v1, g.getPrev(v4, 0));


        assertEquals('x', getArrowId(g.getPrevArrow(v1, 0)));
        assertEquals('c', getArrowId(g.getPrevArrow(v2, 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v3, 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v3, 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v4, 0)));
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
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);

        assertEquals(v1, g.getNext(v1, 0));
        g.removeNext(v1, 0);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v0));
        assertEquals(1, g.getPrevCount(v1));
        assertEquals(1, g.getPrevCount(v2));
        assertEquals(2, g.getPrevCount(v3));
        assertEquals(1, g.getPrevCount(v4));

        assertEquals(v0, g.getPrev(v1, 0));
        assertEquals(v1, g.getPrev(v2, 0));
        assertEquals(v0, g.getPrev(v3, 0));
        assertEquals(v4, g.getPrev(v3, 1));
        assertEquals(v1, g.getPrev(v4, 0));


        assertEquals('a', getArrowId(g.getPrevArrow(v1, 0)));
        assertEquals('c', getArrowId(g.getPrevArrow(v2, 0)));
        assertEquals('b', getArrowId(g.getPrevArrow(v3, 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v3, 1)));
        assertEquals('d', getArrowId(g.getPrevArrow(v4, 0)));
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
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);

        g.removeVertex(v1);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(0, g.getPrevCount(v0));
        assertEquals(0, g.getPrevCount(v2));
        assertEquals(2, g.getPrevCount(v3));
        assertEquals(0, g.getPrevCount(v4));

        assertEquals(0, g.getPrev(v3, 0));
        assertEquals(4, g.getPrev(v3, 1));

        assertEquals('b', getArrowId(g.getPrevArrow(v3, 0)));
        assertEquals('e', getArrowId(g.getPrevArrow(v3, 1)));
    }
}