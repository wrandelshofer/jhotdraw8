/*
 * @(#)SimpleMutableDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;


/**
 * Simple implementation of the {@link MutableDirectedGraph} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public class SimpleMutableDirectedGraph<V, A>
        implements MutableDirectedGraph<V, A>, AttributedIndexedDirectedGraph<V, A> {

    private static final Object TOMBSTONE_OBJECT = new Object();

    private final SimpleMutableIndexedDirectedGraph g;
    /**
     * Maps a vertex to a vertex index.
     */
    private final Map<V, Integer> vertexMap;
    /**
     * Maps a vertex index to a vertex object.
     */
    private final List<V> vertices;
    /**
     * Maps an arrow index to an arrow object. May contain {@link #TOMBSTONE_OBJECT}s.
     */
    private final List<Object> arrows;

    /**
     * Creates a new instance with an initial capacity for 16 vertices and 16 arrows.
     * <p>
     * Uses a non-identity hash map for storing the vertices.
     */
    public SimpleMutableDirectedGraph() {
        this(16, 16, false);
    }

    /**
     * Creates a new instance with the specified initial capacities.
     * <p>
     * Uses a non-identity hash map for storing the vertices.
     *
     * @param vertexCapacity the initial capacity for vertices
     * @param arrowCapacity  the initial capacity for arrows
     */
    public SimpleMutableDirectedGraph(int vertexCapacity, int arrowCapacity) {
        this(vertexCapacity, arrowCapacity, false);
    }

    /**
     * Creates a new instance with the specified initial capacities.
     *
     * @param vertexCapacity the initial capacity for vertices
     * @param arrowCapacity  the initial capacity for arrows
     * @param identityMap    whether to use an identity hash map for storing the vertices
     */
    public SimpleMutableDirectedGraph(int vertexCapacity, int arrowCapacity, boolean identityMap) {
        this.g = new SimpleMutableIndexedDirectedGraph(vertexCapacity, arrowCapacity);
        this.vertexMap = identityMap ? new IdentityHashMap<>(vertexCapacity) : new HashMap<>(vertexCapacity);
        this.vertices = new ArrayList<>(vertexCapacity);
        this.arrows = new ArrayList<>(arrowCapacity);
        this.addVertexIfAbsent = k -> {
            vertices.add(k);
            g.addVertexAsInt();
            return vertices.size() - 1;
        };
    }

    /**
     * Creates a new instance which contains a copy of the specified graph.
     *
     * @param graph a graph
     */
    public SimpleMutableDirectedGraph(DirectedGraph<V, A> graph) {
        this(graph, Function.identity(), (v1, v2, a) -> a);
    }

    /**
     * Creates a new instance which contains a copy of the specified graph.
     * <p>
     * Uses a non-identity hash map for storing the vertices.
     *
     * @param graph        a graph
     * @param vertexMapper a mapping function for the vertices
     * @param arrowMapper  a mapping function for the arrows
     * @param <VV>         the vertex data type of the graph
     * @param <AA>         the arrow data type of the graph
     */
    public <VV, AA> SimpleMutableDirectedGraph(DirectedGraph<VV, AA> graph,
                                               Function<VV, V> vertexMapper,
                                               Function3<VV, VV, AA, A> arrowMapper) {
        this.g = new SimpleMutableIndexedDirectedGraph(graph.getVertexCount(), graph.getArrowCount());
        final int vcount = graph.getVertexCount();
        this.vertexMap = new HashMap<>(vcount);
        this.vertices = new ArrayList<>(vcount);
        this.arrows = new ArrayList<>(graph.getArrowCount());
        this.addVertexIfAbsent = k -> {
            vertices.add(k);
            g.addVertexAsInt();
            return vertices.size() - 1;
        };

        for (VV vv : graph.getVertices()) {
            addVertex(vertexMapper.apply(vv));
        }
        for (VV vv : graph.getVertices()) {
            for (int j = 0, n = graph.getNextCount(vv); j < n; j++) {
                VV next = graph.getNext(vv, j);
                addArrow(vertexMapper.apply(vv),
                        vertexMapper.apply(next),
                        arrowMapper.apply(vv, next, graph.getNextArrow(vv, j)));
            }
        }
    }


    @Override
    public void addArrow(V va, V vb, @Nullable A arrow) {
        Objects.requireNonNull(va, "va");
        Objects.requireNonNull(vb, "vb");
        int a = vertexMap.get(va);
        int b = vertexMap.get(vb);
        int arrowIndex = g.addArrowAsInt(a, b);
        if (arrowIndex == arrows.size()) {
            arrows.add(arrow);
        } else {
            arrows.set(arrowIndex, arrow);
        }
    }


    @Override
    public void removeArrow(V v, V u, @Nullable A a) {
        int vidx = vertexMap.get(v);
        int uidx = vertexMap.get(u);
        int index = 0;
        for (Enumerator.OfInt it = nextVerticesEnumerator(vidx); it.moveNext(); ) {
            int widx = it.currentAsInt();
            if (uidx == widx && Objects.equals(a, this.getNextArrow(vidx, index))) {
                int indexOfRemovedArrow = g.removeNextAsInt(vertexMap.get(v), index);
                arrows.set(indexOfRemovedArrow, TOMBSTONE_OBJECT);
                return;
            }
            index++;
        }
    }

    @Override
    public void removeArrow(V v, V u) {
        Integer vidx = vertexMap.get(v);
        int index = 0;
        for (Enumerator.OfInt it = nextVerticesEnumerator(vidx); it.moveNext(); ) {
            int uidx = it.currentAsInt();
            if (u.equals(vertices.get(uidx))) {
                int indexOfRemovedArrow = g.removeNextAsInt(vertexMap.get(v), index);
                arrows.set(indexOfRemovedArrow, TOMBSTONE_OBJECT);
                return;
            }
            index++;
        }
    }

    @Override
    public void removeNext(V v, int k) {
        int indexOfRemovedArrow = g.removeNextAsInt(vertexMap.get(v), k);
        arrows.set(indexOfRemovedArrow, TOMBSTONE_OBJECT);
    }

    /**
     * Adds an arrow from 'va' to 'vb' and an arrow from 'vb' to 'va'.
     *
     * @param va    vertex a
     * @param vb    vertex b
     * @param arrow the arrow
     */
    public void addBidiArrow(V va, V vb, A arrow) {
        addArrow(va, vb, arrow);
        addArrow(vb, va, arrow);
    }

    /**
     * Adds a vertex.
     *
     * @param v vertex
     */
    @Override
    public void addVertex(V v) {
        Objects.requireNonNull(v, "v");
        vertexMap.computeIfAbsent(v, addVertexIfAbsent);
    }

    /**
     * Adds a vertex at the specified index.
     *
     * @param v    vertex
     * @param vidx vertex index
     */
    public void addVertex(V v, int vidx) {
        Objects.requireNonNull(v, "v");
        g.insertVertexAt(vidx);
        vertices.add(vidx, v);
        for (Map.Entry<V, Integer> entry : vertexMap.entrySet()) {
            Integer uidx = entry.getValue();
            if (uidx >= vidx) {
                entry.setValue(uidx + 1);
            }
        }
        vertexMap.put(v, vidx);
    }

    @Override
    public void removeVertex(V v) {
        Integer vidxBox = vertexMap.remove(v);
        if (vidxBox == null) {
            return;
        }
        int vidx = vidxBox;
        // Remove all outgoing vertices
        for (int i = getNextCount(vidx) - 1; i >= 0; i--) {
            int indexOfRemovedArrow = g.removeNextAsInt(vidx, i);
            arrows.set(indexOfRemovedArrow, TOMBSTONE_OBJECT);
        }
        // Remove all incoming vertices
        for (int uidx = 0, n = getVertexCount(); uidx < n; uidx++) {
            for (int i = getNextCount(uidx) - 1; i >= 0; i--) {
                int next = getNextAsInt(uidx, i);
                if (next == vidx) {
                    int indexOfRemovedArrow = g.removeNextAsInt(uidx, i);
                    arrows.set(indexOfRemovedArrow, TOMBSTONE_OBJECT);
                }
            }
        }
        g.removeVertexAfterArrowsHaveBeenRemoved(vidx);
        vertices.remove(vidx);
        for (Map.Entry<V, Integer> entry : vertexMap.entrySet()) {
            Integer uidx = entry.getValue();
            if (uidx > vidx) {
                entry.setValue(uidx - 1);
            }
        }
    }

    /**
     * Performance: We need this lambda very often if a large graph is created
     * with this builder.
     */
    private final Function<V, Integer> addVertexIfAbsent;


    public void clear() {
        g.clear();
        vertexMap.clear();
        vertices.clear();
        arrows.clear();
    }

    @Override
    public A getNextArrow(V v, int index) {
        int arrowId = g.getNextArrowIndex(getVertexIndex(v), index);
        @SuppressWarnings("unchecked")
        A a = (A) arrows.get(arrowId);
        return a;
    }

    @Override
    public V getNext(V v, int i) {
        return getVertex(getNextAsInt(getVertexIndex(v), i));
    }

    @Override
    public int getNextCount(V v) {
        return getNextCount(getVertexIndex(v));
    }

    @Override
    public int getVertexCount() {
        return g.getVertexCount();
    }

    @Override
    public int getArrowCount() {
        return g.getArrowCount();
    }

    @Override
    public int getNextAsInt(int v, int i) {
        return g.getNextAsInt(v, i);
    }

    @Override
    public V getVertex(int vi) {
        if (vertices.get(vi) == null) {
            System.err.println("DIrectedGraphBuilder is broken");
        }
        return vertices.get(vi);
    }

    @Override
    public int getVertexIndex(V v) {
        Integer index = vertexMap.get(v);
        return index;
    }

    @Override
    public A getArrow(int index) {
        // This has quadratic performance!
        int i = index;
        int vidx = 0;
        int nextCount = getNextCount(vidx);
        while (i >= nextCount) {
            vidx++;
            i -= nextCount;
            nextCount = getNextCount(vidx);
        }
        return this.getNextArrow(vidx, i);
    }

    @Override
    @SuppressWarnings("unchecked")
    public A getNextArrow(int v, int index) {
        int arrowId = g.getNextArrowIndex(v, index);
        return (A) arrows.get(arrowId);
    }


    @Override
    public int getNextArrowAsInt(int v, int i) {
        return g.getNextArrowAsInt(v, i);
    }

    @Override
    public int getNextCount(int v) {
        return g.getNextCount(v);
    }

    @Override
    public Set<V> getVertices() {
        return new SetFacade<>(vertices::iterator, vertices::spliterator, vertices::size, vertices::contains, null, null, null);
    }


    public void setOrdered(boolean b) {
        g.setOrdered(b);
    }

    @Override
    public Enumerator.OfInt nextVerticesEnumerator(int v) {
        return g.nextVerticesEnumerator(v);
    }
}
