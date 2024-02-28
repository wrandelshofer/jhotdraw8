/*
 * @(#)AbstractMutableDirectedGraphTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Base class for tests that test implementations of the {@link MutableDirectedGraph}
 * interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public abstract class AbstractMutableDirectedGraphTest<V, A> {
    protected abstract MutableDirectedGraph<V, A> newInstance();

    protected abstract MutableDirectedGraph<V, A> newInstance(DirectedGraph<V, A> g);

    protected abstract @NonNull V newVertex(int id);

    protected abstract @NonNull A newArrow(@NonNull V start, @NonNull V end, char id);

    /**
     * Returns the arrow id. Returns \u0000 if the arrow is null.
     */
    protected abstract char getArrowId(@Nullable A a);

    protected abstract int getVertexId(@NonNull V v);

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
    protected MutableDirectedGraph<V, A> buildGraph() {
        MutableDirectedGraph<V, A> g = newInstance();
        V v0 = newVertex(0);
        V v1 = newVertex(1);
        V v2 = newVertex(2);
        V v3 = newVertex(3);
        V v4 = newVertex(4);
        g.addVertex(v0);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addArrow(v0, v1, newArrow(v0, v1, 'a'));
        g.addArrow(v0, v3, newArrow(v0, v3, 'b'));
        g.addArrow(v1, v1, newArrow(v1, v1, 'x'));
        g.addArrow(v1, v2, newArrow(v1, v2, 'c'));
        g.addArrow(v1, v4, newArrow(v1, v4, 'd'));
        g.addArrow(v4, v3, newArrow(v4, v3, 'e'));
        return g;
    }

    /**
     * Test getters.
     */
    @Test
    public void testAddVerticesAndArrows() {
        MutableDirectedGraph<V, A> g = buildGraph();

        assertEqualsToInitialGraph(g);
    }

    /**
     * Test copy.
     */
    @Test
    public void testCopy() {
        MutableDirectedGraph<V, A> g = newInstance(buildGraph());

        assertEqualsToInitialGraph(g);
    }

    private void assertEqualsToInitialGraph(@NonNull MutableDirectedGraph<V, A> g) {
        assertEquals(5, g.getVertexCount());
        assertEquals(6, g.getArrowCount());
        V[] v = getVertices(g);

        assertEquals(2, g.getNextCount(v[0]));
        assertEquals(3, g.getNextCount(v[1]));
        assertEquals(0, g.getNextCount(v[2]));
        assertEquals(0, g.getNextCount(v[3]));
        assertEquals(1, g.getNextCount(v[4]));

        SequencedSet<V> actualVertices = new LinkedHashSet<>(g.getNextVertices(v[0]));
        assertEquals(Set.of(v[1], v[3]), actualVertices);
        assertEquals(v[1], g.getNext(v[1], 0));
        assertEquals(v[2], g.getNext(v[1], 1));
        assertEquals(v[4], g.getNext(v[1], 2));
        assertEquals(v[3], g.getNext(v[4], 0));

        actualVertices = new LinkedHashSet<>(g.getNextVertices(v[0]));
        assertEquals(Set.of(v[1], v[3]), actualVertices);
        actualVertices = new LinkedHashSet<>(g.getNextVertices(v[1]));
        assertEquals(Set.of(v[1], v[2], v[4]), actualVertices);
        actualVertices = new LinkedHashSet<>(g.getNextVertices(v[2]));
        assertEquals(Set.of(), actualVertices);
        actualVertices = new LinkedHashSet<>(g.getNextVertices(v[3]));
        assertEquals(Set.of(), actualVertices);
        actualVertices = new LinkedHashSet<>(g.getNextVertices(v[4]));
        assertEquals(Set.of(v[3]), actualVertices);

        if (g instanceof IndexedDirectedGraph) {
            IndexedDirectedGraph ig = (IndexedDirectedGraph) g;
            Set<Integer> actualIndices;
            actualIndices = StreamSupport.intStream(ig.nextVerticesEnumerator(0), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(1, 3), actualIndices);
            actualIndices = StreamSupport.intStream(ig.nextVerticesEnumerator(1), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(1, 2, 4), actualIndices);
            actualIndices = StreamSupport.intStream(ig.nextVerticesEnumerator(2), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(), actualIndices);
            actualIndices = StreamSupport.intStream(ig.nextVerticesEnumerator(3), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(), actualIndices);
            actualIndices = StreamSupport.intStream(ig.nextVerticesEnumerator(4), false).boxed().collect(Collectors.toSet());
            assertEquals(Set.of(3), actualIndices);
        }


        Set<Character> actualArrows = new HashSet<>(g.getNextArrows(v[0])).stream().map(this::getArrowId).collect(Collectors.toSet());
        assertEquals(Set.of('a', 'b'), actualArrows);
        assertEquals('x', getArrowId(g.getNextArrow(v[1], 0)));
        assertEquals('c', getArrowId(g.getNextArrow(v[1], 1)));
        assertEquals('d', getArrowId(g.getNextArrow(v[1], 2)));
        assertEquals('e', getArrowId(g.getNextArrow(v[4], 0)));

        assertEquals(Set.of(v[0], v[1], v[2], v[3], v[4]), g.getVertices());
        assertEquals(Set.of('a', 'b', 'x', 'c', 'd', 'e'), new LinkedHashSet<>(g.getArrows()).stream().map(this::getArrowId).collect(Collectors.toSet()));

        assertEquals(Set.of(v[0], v[1], v[2], v[3], v[4]), g.getVertices());
    }

    protected V[] getVertices(MutableDirectedGraph<V, A> g) {
        @SuppressWarnings("unchecked")
        V[] vertices = (V[]) new Object[g.getVertexCount()];
        for (V v : g.getVertices()) {
            assertNull(vertices[getVertexId(v)]);
            vertices[getVertexId(v)] = v;
        }
        return vertices;
    }

    /**
     * Test arrow removal.
     */
    @Test
    public void testRemoveArrow() {
        MutableDirectedGraph<V, A> g = buildGraph();
        V[] v = getVertices(g);
        Collection<A> arrows = g.getArrows(v[0], v[1]);
        assertEquals(1, arrows.size());

        A a = arrows.iterator().next();
        g.removeArrow(v[0], v[1], a);

        continueTestRemoveArrow(g);
    }

    /**
     * Test arrow removal.
     */
    @Test
    public void testRemoveArrowWithoutData() {
        MutableDirectedGraph<V, A> g = buildGraph();
        V[] v = getVertices(g);

        g.removeArrow(v[0], v[1]);

        continueTestRemoveArrow(g);
    }

    private void continueTestRemoveArrow(MutableDirectedGraph<V, A> g) {
        V[] v = getVertices(g);

        assertEquals(5, g.getVertexCount());
        assertEquals(5, g.getArrowCount());

        assertEquals(1, g.getNextCount(v[0]));
        assertEquals(3, g.getNextCount(v[1]));
        assertEquals(0, g.getNextCount(v[2]));
        assertEquals(0, g.getNextCount(v[3]));
        assertEquals(1, g.getNextCount(v[4]));

        assertEquals(v[3], g.getNext(v[0], 0));
        assertEquals(v[1], g.getNext(v[1], 0));
        assertEquals(v[2], g.getNext(v[1], 1));
        assertEquals(v[4], g.getNext(v[1], 2));
        assertEquals(v[3], g.getNext(v[4], 0));

        assertEquals('b', getArrowId(g.getNextArrow(v[0], 0)));
        assertEquals('x', getArrowId(g.getNextArrow(v[1], 0)));
        assertEquals('c', getArrowId(g.getNextArrow(v[1], 1)));
        assertEquals('d', getArrowId(g.getNextArrow(v[1], 2)));
        assertEquals('e', getArrowId(g.getNextArrow(v[4], 0)));

        assertEquals(Set.of(v[0], v[1], v[2], v[3], v[4]), g.getVertices());
        Collection<Character> actualArrows = g.getArrows().stream().map(this::getArrowId).collect(Collectors.toList());
        assertEquals(List.of('b', 'x', 'c', 'd', 'e'), actualArrows);

        g.addArrow(v[0], v[1], newArrow(v[0], v[1], 'a'));
        assertEqualsToInitialGraph(g);
    }

    /**
     * Test arrowAt removal.
     */
    @Test
    public void testRemoveArrowAt() {
        MutableDirectedGraph<V, A> g = buildGraph();
        V[] v = getVertices(g);
        g.removeNext(v[0], 0);
        continueTestRemoveArrow(g);
    }

    /**
     * Test remove vertex 1. Vertex 1 has a self-loop.
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
    public void testRemoveVertexWithSelfLoop() {
        MutableDirectedGraph<V, A> g = buildGraph();
        V[] v = getVertices(g);

        g.removeVertex(v[1]);

        assertEquals(4, g.getVertexCount());
        assertEquals(2, g.getArrowCount());

        assertEquals(1, g.getNextCount(v[0]));
        assertEquals(0, g.getNextCount(v[2]));
        assertEquals(0, g.getNextCount(v[3]));
        assertEquals(1, g.getNextCount(v[4]));

        assertEquals(v[3], g.getNext(v[0], 0));
        assertEquals(v[3], g.getNext(v[4], 0));

        assertEquals('b', getArrowId(g.getNextArrow(v[0], 0)));
        assertEquals('e', getArrowId(g.getNextArrow(v[4], 0)));

        assertEquals(Set.of(v[0], v[2], v[3], v[4]), g.getVertices());
        assertEquals(List.of('b', 'e'), g.getArrows().stream().map(this::getArrowId).collect(Collectors.toList()));

        if (g instanceof SimpleMutableDirectedGraph) {
            ((SimpleMutableDirectedGraph<V, A>) g).addVertex(v[1], 1);
        } else {
            g.addVertex(v[1]);
        }
        g.addArrow(v[0], v[1], newArrow(v[0], v[1], 'a'));
        g.addArrow(v[1], v[1], newArrow(v[1], v[1], 'x'));
        g.addArrow(v[1], v[2], newArrow(v[1], v[2], 'c'));
        g.addArrow(v[1], v[4], newArrow(v[1], v[4], 'd'));
        assertEqualsToInitialGraph(g);

    }
}