/*
 * @(#)AbstractMutableBidiGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractMutableBidiGraphTest
        extends AbstractMutableDirectedGraphTest {
    protected abstract MutableBidiGraph<Integer, Character> newInstance();

    protected abstract MutableBidiGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g);

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
        MutableBidiGraph<Integer, Character> g = (MutableBidiGraph<Integer, Character>) buildGraph();
        assertEqualsInitialGraph(g);
    }

    @Test
    public void testCopy() {
        MutableBidiGraph<Integer, Character> g = newInstance((MutableBidiGraph<Integer, Character>) buildGraph());

        assertEqualsInitialGraph(g);
    }

    private void assertEqualsInitialGraph(MutableBidiGraph<Integer, Character> g) {
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

        assertEquals('a', g.getPrevArrow(1, 0));
        assertEquals('x', g.getPrevArrow(1, 1));
        assertEquals('c', g.getPrevArrow(2, 0));
        assertEquals('b', g.getPrevArrow(3, 0));
        assertEquals('e', g.getPrevArrow(3, 1));
        assertEquals('d', g.getPrevArrow(4, 0));
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
        MutableBidiGraph<Integer, Character> g = (MutableBidiGraph<Integer, Character>) buildGraph();

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

        assertEquals('x', g.getPrevArrow(1, 0));
        assertEquals('c', g.getPrevArrow(2, 0));
        assertEquals('b', g.getPrevArrow(3, 0));
        assertEquals('e', g.getPrevArrow(3, 1));
        assertEquals('d', g.getPrevArrow(4, 0));
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
        MutableBidiGraph<Integer, Character> g = (MutableBidiGraph<Integer, Character>) buildGraph();

        g.removeNext(0, 0);

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


        assertEquals('x', g.getPrevArrow(1, 0));
        assertEquals('c', g.getPrevArrow(2, 0));
        assertEquals('b', g.getPrevArrow(3, 0));
        assertEquals('e', g.getPrevArrow(3, 1));
        assertEquals('d', g.getPrevArrow(4, 0));
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
        MutableBidiGraph<Integer, Character> g = (MutableBidiGraph<Integer, Character>) buildGraph();

        g.removeVertex(1);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(0, g.getPrevCount(0));
        assertEquals(0, g.getPrevCount(2));
        assertEquals(2, g.getPrevCount(3));
        assertEquals(0, g.getPrevCount(4));

        assertEquals(0, g.getPrev(3, 0));
        assertEquals(4, g.getPrev(3, 1));

        assertEquals('b', g.getPrevArrow(3, 0));
        assertEquals('e', g.getPrevArrow(3, 1));
    }
}