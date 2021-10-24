/*
 * @(#)AbstractDirectedGraphBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * AbstractDirectedGraphBuilder.
 * <p>
 * <b>Implementation:</b>
 * <p>
 * Example graph:
 * <pre>
 *     0 ──→ 1 ──→ 2
 *     │     │
 *     ↓     ↓
 *     3 ←── 4
 * </pre>
 * If the graph is inserted in the following sequence
 * into the builder:
 * <pre>
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     buildAddVertex();
 *     build.addArrow(0, 1);
 *     build.addArrow(0, 3);
 *     build.addArrow(1, 2);
 *     build.addArrow(1, 4);
 *     build.addArrow(4, 3);
 * </pre>
 * Then the internal representation is as follows:
 * <pre>
 *     vertexCount: 5
 *
 *  vertex#    nodes
 *
 *    0        Node{1,3}
 *    1        Node{2,4}
 *    2        null
 *    3        null
 *    4        Node{3}
 * </pre>
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractMutableDirectedGraph<V, A>
        implements IntDirectedGraph, DirectedGraph<V, A> {
    private Node<A>[] nodes;
    private int vertexCount;
    private int arrowCount;
    private @NonNull List<V> vertices;
    /**
     * Performance: We need this lambda very often if a large graph is created
     * with this builder.
     */
    private final Function<V, Integer> addVertexIfAbsent = k -> {
        vertices.add(k);
        buildAddVertex();
        return vertices.size() - 1;
    };
    private @NonNull
    final Map<V, Integer> vertexMap;
    private @Nullable List<A> cachedArrows;

    public AbstractMutableDirectedGraph() {
        this(0, 0, false);
    }

    public AbstractMutableDirectedGraph(@NonNull DirectedGraph<V, A> g) {
        this(g.getVertexCount(), g.getArrowCount(), false);
        for (V v : g.getVertices()) {
            buildAddVertex(v);
        }
        for (V v : getVertices()) {
            for (Arc<V, A> arc : g.getNextArcs(v)) {
                buildAddArrow(v, arc.getEnd(), arc.getData());
            }
        }
    }

    public AbstractMutableDirectedGraph(int vertexCapacity, int arrowCapacity, boolean useIdentityMap) {
        nodes = (Node<A>[]) new Node<?>[vertexCapacity];
        vertices = new ArrayList<>(vertexCapacity);
        vertexMap = useIdentityMap ? new IdentityHashMap<>(vertexCapacity) : new LinkedHashMap<>(vertexCapacity * 2);
    }

    /**
     * Builder-method: adds a directed arrow from 'v' to 'u'.
     *
     * @param vidx index of v
     * @param uidx index of u
     */
    protected void buildAddArrow(int vidx, int uidx, A data) {
        Node<A> node = nodes[vidx];
        if (node == null) {
            node = nodes[vidx] = new Node<>();
        }
        node.next.add(uidx);
        node.nextData.add(data);
        arrowCount++;
        cachedArrows = null;
    }

    protected void buildAddArrow(V v, V u, A data) {
        Integer vidx = vertexMap.get(v);
        Integer uidx = vertexMap.get(u);
        if (vidx == null) {
            throw new NoSuchElementException("v: " + v);
        }
        if (uidx == null) {
            throw new NoSuchElementException("u: " + u);
        }
        buildAddArrow(vidx, uidx, data);
    }

    protected void buildAddVertex() {
        vertexCount++;
        if (nodes.length < vertexCount) {
            nodes = Arrays.copyOf(nodes, vertexCount * 2);
        }
    }

    protected void buildAddVertex(V v) {
        Objects.requireNonNull(v, "v");
        vertexMap.computeIfAbsent(v, addVertexIfAbsent);
    }

    /**
     * Removes an arrow from v to u.
     *
     * @param vidx index of v
     * @param uidx index of u
     */
    protected void buildRemoveArrow(int vidx, int uidx, @Nullable A a) {
        Node<A> node = nodes[vidx];
        if (node == null) {
            throw new IndexOutOfBoundsException(vidx);
        }
        for (int i = node.next.indexOf(uidx); i != -1; i = node.next.indexOf(uidx, i + 1)) {
            Object ua = node.nextData.get(i);
            if (Objects.equals(ua, a)) {
                buildRemoveArrowAt(vidx, node.next.indexOf(uidx));
                return;
            }
        }
        throw new NoSuchElementException("vidx: " + vidx + " uidx: " + uidx + " a: " + a);
    }

    protected void buildRemoveArrow(int vidx, int uidx) {
        Node<A> node = nodes[vidx];
        if (node == null) {
            throw new IndexOutOfBoundsException(vidx);
        }
        int i = node.next.indexOf(uidx);
        if (i != -1) {
            buildRemoveArrowAt(vidx, node.next.indexOf(uidx));
            return;
        }
        throw new NoSuchElementException("vidx: " + vidx + " uidx: " + uidx);
    }

    protected void buildRemoveArrow(V v, V u, A data) {
        Integer vidx = vertexMap.get(v);
        Integer uidx = vertexMap.get(u);
        if (vidx == null) {
            throw new NoSuchElementException("vidx: " + vidx);
        }
        if (uidx == null) {
            throw new NoSuchElementException("vidx: " + vidx);
        }
        try {
            buildRemoveArrow(vidx, uidx, data);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("v: " + v + " u: " + u + " a: " + data);
        }
    }

    protected void buildRemoveArrow(V v, V u) {
        Integer vidx = vertexMap.get(v);
        Integer uidx = vertexMap.get(u);
        if (vidx == null) {
            throw new NoSuchElementException("vidx: " + vidx);
        }
        if (uidx == null) {
            throw new NoSuchElementException("vidx: " + vidx);
        }
        try {
            buildRemoveArrow(vidx, uidx);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("v: " + v + " u: " + u);
        }
    }

    /**
     * Removes the i-th arrow of vertex v.
     *
     * @param vidx index of v
     * @param i    the i-th arrow of the vertex
     */
    protected void buildRemoveArrowAt(int vidx, int i) {
        Node<A> node = nodes[vidx];
        if (node == null) {
            throw new IndexOutOfBoundsException(i);
        }
        node.next.removeAt(i);
        node.nextData.remove(i);
        if (node.isNodeEmpty()) {
            nodes[vidx] = null;
        }
        arrowCount--;
        cachedArrows = null;
    }

    protected void buildRemoveArrowAt(V v, int k) {
        Integer vidx = vertexMap.get(v);
        buildRemoveArrowAt(vidx, k);
    }

    protected void buildRemoveVertex(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        Node<A> vnode = nodes[vidx];
        arrowCount -= vnode.next.size();
        if (vidx < vertexCount - 1) {
            System.arraycopy(nodes, vidx + 1, nodes, vidx, vertexCount - vidx);
        }
        nodes[vertexCount - 1] = null;
        vertexCount--;
        for (int i = 0, nodesLength = nodes.length; i < nodesLength; i++) {
            Node<A> node = nodes[i];
            if (node != null) {
                for (int j = node.next.size() - 1; j >= 0; j--) {
                    int uidx = node.next.get(j);
                    if (uidx == vidx) {
                        node.next.removeAt(j);
                        node.nextData.remove(j);
                        arrowCount--;
                    } else if (uidx > vidx) {
                        node.next.set(j, uidx - 1);
                    }
                }
                if (node.isNodeEmpty()) {
                    nodes[i] = null;
                }
            }
        }
        cachedArrows = null;
    }

    protected void buildRemoveVertex(V v) {
        Integer vidx = vertexMap.remove(v);
        if (vidx == null) {
            throw new NoSuchElementException("" + v);
        }
        buildRemoveVertex(vidx);
        vertices.remove((int) vidx);
        for (Map.Entry<V, Integer> entry : vertexMap.entrySet()) {
            Integer uidx = entry.getValue();
            if (uidx > vidx) {
                entry.setValue(uidx - 1);
            }
        }
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public @NonNull Collection<A> getArrows() {
        if (cachedArrows == null) {
            List<A> list = new ArrayList<>(arrowCount);
            for (Node<A> node : nodes) {
                if (node != null) {
                    for (int i = 0, n = node.nextData.size(); i < n; i++) {
                        list.add(node.nextData.get(i));
                    }
                }
            }
            this.cachedArrows = list;
        }
        return Collections.unmodifiableList(cachedArrows);
    }

    @Override
    public int getNext(int vidx, int k) {
        Node<A> node = nodes[vidx];
        if (node == null) {
            throw new IndexOutOfBoundsException(k);
        }
        return node.next.get(k);
    }

    @Override
    public @NonNull V getNext(@NonNull V vertex, int index) {
        return vertices.get(getNext(vertexMap.get(vertex), index));
    }

    @Override
    public @NonNull A getNextArrow(@NonNull V vertex, int index) {
        Node<A> node = nodes[vertexMap.get(vertex)];
        if (node == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return node.nextData.get(index);
    }

    @Override
    public int getNextCount(@NonNull V vertex) {
        return getNextCount(vertexMap.get(vertex));
    }

    @Override
    public int getNextCount(int vidx) {
        Node<A> node = nodes[vidx];
        return node == null ? 0 : node.next.size();
    }

    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return Collections.unmodifiableSet(vertexMap.keySet());
    }

    @Override
    public V getVertex(int index) {
        return vertices.get(index);
    }

    protected static class Node<A> {
        final IntArrayList next = new IntArrayList();
        final List<A> nextData = new ArrayList<>();

        public boolean isNodeEmpty() {
            return next.isEmpty();
        }
    }
}
