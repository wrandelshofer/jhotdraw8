package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractMutableDirectedGraphTest {
    protected abstract MutableDirectedGraph<Integer, Character> newInstance();

    protected abstract MutableDirectedGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g);

    /**
     * Example graph:
     * <pre>
     *            x
     *           ⟲
     *     0 ─a→ 1 ─c→ 2
     *     │     │
     *     b     d
     *     ↓     ↓
     *     3 ←e─ 4
     * </pre>
     */
    @NonNull
    protected MutableDirectedGraph<Integer, Character> buildGraph() {
        MutableDirectedGraph<Integer, Character> g = newInstance();
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addArrow(0, 1, 'a');
        g.addArrow(0, 3, 'b');
        g.addArrow(1, 1, 'x');
        g.addArrow(1, 2, 'c');
        g.addArrow(1, 4, 'd');
        g.addArrow(4, 3, 'e');
        return g;
    }

    /**
     * Test getters.
     */
    @Test
    public void testAddVerticesAndArrows() {
        MutableDirectedGraph<Integer, Character> g = buildGraph();

        assertEqualsToInitialGraph(g);
    }

    /**
     * Test copy.
     */
    @Test
    public void testCopy() {
        MutableDirectedGraph<Integer, Character> g = newInstance(buildGraph());

        assertEqualsToInitialGraph(g);
    }

    private void assertEqualsToInitialGraph(MutableDirectedGraph<Integer, Character> g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());

        assertEquals(2, g.getNextCount(0));
        assertEquals(3, g.getNextCount(1));
        assertEquals(0, g.getNextCount(2));
        assertEquals(0, g.getNextCount(3));
        assertEquals(1, g.getNextCount(4));

        Set<Integer> actualVertices = new HashSet<>(g.getNextVertices(0));
        assertEquals(Set.of(1, 3), actualVertices);
        assertEquals(1, g.getNext(1, 0));
        assertEquals(2, g.getNext(1, 1));
        assertEquals(4, g.getNext(1, 2));
        assertEquals(3, g.getNext(4, 0));

        actualVertices = new HashSet<>(g.getNextVertices(0));
        assertEquals(Set.of(1, 3), actualVertices);
        actualVertices = new HashSet<>(g.getNextVertices(1));
        assertEquals(Set.of(1, 2, 4), actualVertices);
        actualVertices = new HashSet<>(g.getNextVertices(2));
        assertEquals(Set.of(), actualVertices);
        actualVertices = new HashSet<>(g.getNextVertices(3));
        assertEquals(Set.of(), actualVertices);
        actualVertices = new HashSet<>(g.getNextVertices(4));
        assertEquals(Set.of(3), actualVertices);

        if (g instanceof IndexedDirectedGraph) {
            IndexedDirectedGraph ig = (IndexedDirectedGraph) g;
            actualVertices = StreamSupport.intStream(ig.nextVerticesSpliterator(0), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(1, 3), actualVertices);
            actualVertices = StreamSupport.intStream(ig.nextVerticesSpliterator(1), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(1, 2, 4), actualVertices);
            actualVertices = StreamSupport.intStream(ig.nextVerticesSpliterator(2), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(), actualVertices);
            actualVertices = StreamSupport.intStream(ig.nextVerticesSpliterator(3), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(), actualVertices);
            actualVertices = StreamSupport.intStream(ig.nextVerticesSpliterator(4), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(3), actualVertices);
        }

        Set<Character> actualArrows = new HashSet<>(g.getNextArrows(0));
        assertEquals(Set.of('a', 'b'), actualArrows);
        assertEquals('x', g.getNextArrow(1, 0));
        assertEquals('c', g.getNextArrow(1, 1));
        assertEquals('d', g.getNextArrow(1, 2));
        assertEquals('e', g.getNextArrow(4, 0));

        assertEquals(Set.of(0, 1, 2, 3, 4), g.getVertices());
        assertEquals(Set.of('a', 'b', 'x', 'c', 'd', 'e'), new LinkedHashSet<>(g.getArrows()));

        actualVertices.clear();
        for (int i = 0, n = g.getVertexCount(); i < n; i++) {
            actualVertices.add(g.getVertex(i));
        }
        assertEquals(Set.of(0, 1, 2, 3, 4), actualVertices);
    }

    /**
     * Test arrow removal.
     */
    @Test
    public void testRemoveArrow() {
        MutableDirectedGraph<Integer, Character> g = buildGraph();

        g.removeArrow(0, 1, 'a');

        continueTestRemoveArrow(g);
    }

    /**
     * Test arrow removal.
     */
    @Test
    public void testRemoveArrowWithoutData() {
        MutableDirectedGraph<Integer, Character> g = buildGraph();

        g.removeArrow(0, 1);

        continueTestRemoveArrow(g);
    }

    private void continueTestRemoveArrow(MutableDirectedGraph<Integer, Character> g) {
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

        assertEquals('b', g.getNextArrow(0, 0));
        assertEquals('x', g.getNextArrow(1, 0));
        assertEquals('c', g.getNextArrow(1, 1));
        assertEquals('d', g.getNextArrow(1, 2));
        assertEquals('e', g.getNextArrow(4, 0));

        assertEquals(Set.of(0, 1, 2, 3, 4), g.getVertices());
        Collection<Character> actualArrows = g.getArrows();
        assertEquals(List.of('b', 'x', 'c', 'd', 'e'), actualArrows);

        g.addArrow(0, 1, 'a');
        assertEqualsToInitialGraph(g);
    }

    /**
     * Test arrowAt removal.
     */
    @Test
    public void testRemoveArrowAt() {
        MutableDirectedGraph<Integer, Character> g = buildGraph();
        g.removeNext(0, 0);
        continueTestRemoveArrow(g);
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
    public void testRemoveVertex() {
        MutableDirectedGraph<Integer, Character> g = buildGraph();

        g.removeVertex(1);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(1, g.getNextCount(0));
        assertEquals(0, g.getNextCount(2));
        assertEquals(0, g.getNextCount(3));
        assertEquals(1, g.getNextCount(4));

        assertEquals(3, g.getNext(0, 0));
        assertEquals(3, g.getNext(4, 0));

        assertEquals('b', g.getNextArrow(0, 0));
        assertEquals('e', g.getNextArrow(4, 0));

        assertEquals(Set.of(0, 2, 3, 4), g.getVertices());
        assertEquals(List.of('b', 'e'), g.getArrows());

        if (g instanceof SimpleMutableDirectedGraph) {
            ((SimpleMutableDirectedGraph<Integer, Character>) g).addVertex(1, 1);
        } else {
            g.addVertex(1);
        }
        g.addArrow(0, 1, 'a');
        g.addArrow(1, 1, 'x');
        g.addArrow(1, 2, 'c');
        g.addArrow(1, 4, 'd');
        assertEqualsToInitialGraph(g);

    }
}