/*
 * @(#)SimpleMutableIndexedBidiGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.primitive.IntArrayList;

import java.util.Arrays;
import java.util.Objects;

/**
 * A simple implementation of the {@link MutableIndexedDirectedGraph} interface.
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
 *    0        Node.next{1,3}
 *    1        Node.next{2,4}.prev{0}
 *    2        Node.prev{1}
 *    3        Node.prev{0}.prev{4}
 *    4        Node.next{3}.prev{1}
 * </pre>
 *
 */

public class SimpleMutableIndexedBidiGraph implements IndexedBidiGraph {
    private Node[] nodes = new Node[0];
    private int vertexCount;
    private int arrowCount;

    public SimpleMutableIndexedBidiGraph() {
    }

    public SimpleMutableIndexedBidiGraph(int vertexCount) {
        addVerticesAsInt(vertexCount);
    }

    public SimpleMutableIndexedBidiGraph(IndexedDirectedGraph g) {
        addVerticesAsInt(g.getVertexCount());
        for (int v = 0; v < vertexCount; v++) {
            for (Enumerator.OfInt it = g.nextVerticesEnumerator(v); it.moveNext(); ) {
                addArrowAsInt(v, it.currentAsInt());
            }
        }
    }

    protected void addVertexAsInt() {
        vertexCount++;
        if (nodes.length < vertexCount) {
            nodes = Arrays.copyOf(nodes, vertexCount * 2);
        }
    }

    protected void addVerticesAsInt(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count: " + count);
        }
        vertexCount += count;
        if (nodes.length < vertexCount) {
            nodes = Arrays.copyOf(nodes, vertexCount * 2);
        }
    }

    protected void removeVertexAsInt(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        Node vnode = nodes[vidx];
        arrowCount -= vnode.next.size();
        if (vidx < vertexCount - 1) {
            System.arraycopy(nodes, vidx + 1, nodes, vidx, vertexCount - vidx);
        }
        nodes[vertexCount - 1] = null;
        vertexCount--;
        for (int i = 0, nodesLength = nodes.length; i < nodesLength; i++) {
            Node node = nodes[i];
            if (node != null) {
                for (int j = node.next.size() - 1; j >= 0; j--) {
                    int uidx = node.next.getAsInt(j);
                    if (uidx == vidx) {
                        node.next.removeAtAsInt(j);
                        arrowCount--;
                    } else if (uidx > vidx) {
                        node.next.setAsInt(j, uidx - 1);
                    }
                }
                for (int j = node.prev.size() - 1; j >= 0; j--) {
                    int uidx = node.prev.getAsInt(j);
                    if (uidx == vidx) {
                        node.prev.removeAtAsInt(j);
                    } else if (uidx > vidx) {
                        node.prev.setAsInt(j, uidx - 1);
                    }
                }
                if (node.isNodeEmpty()) {
                    nodes[i] = null;
                }
            }
        }
    }

    /**
     * Builder-method: adds a directed arrow from 'v' to 'u'.
     *
     * @param vidx index of v
     * @param uidx index of u
     */
    protected void addArrowAsInt(int vidx, int uidx) {
        Node node = nodes[vidx];
        if (node == null) {
            node = nodes[vidx] = new Node();
        }
        node.next.addAsInt(uidx);

        node = nodes[uidx];
        if (node == null) {
            node = nodes[uidx] = new Node();
        }
        node.prev.addAsInt(vidx);

        arrowCount++;
    }

    /**
     * Removes an arrow from v to u.
     *
     * @param vidx index of v
     * @param uidx index of u
     */
    protected void removeArrowAsInt(int vidx, int uidx) {
        Node node = nodes[vidx];
        if (node == null) {
            throw new IndexOutOfBoundsException("vidx=" + vidx);
        }
        removeArrowAtAsInt(vidx, node.next.indexOfAsInt(uidx));
    }

    /**
     * Removes the i-th arrow of vertex v.
     *
     * @param vidx index of v
     * @param i    the i-th arrow of the vertex
     */
    protected void removeArrowAtAsInt(int vidx, int i) {
        Node vnode = nodes[vidx];
        if (vnode == null) {
            throw new IndexOutOfBoundsException("vidx=" + vidx + ", i=" + i);
        }
        int uidx = vnode.next.removeAtAsInt(i);
        if (vnode.isNodeEmpty()) {
            nodes[vidx] = null;
        }

        Node unode = nodes[uidx];
        if (unode == null) {
            throw new IndexOutOfBoundsException("vidx=" + vidx + ", i=" + i);
        }
        unode.prev.removeAtAsInt(unode.prev.indexOfAsInt(vidx));
        if (unode.isNodeEmpty()) {
            nodes[uidx] = null;
        }
        arrowCount--;
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(int v, int index) {
        Node node = nodes[v];
        if (node == null) {
            throw new IndexOutOfBoundsException("vidx=" + v + ", k=" + index);
        }
        return node.next.getAsInt(index);
    }

    @Override
    public int getNextArrowAsInt(int v, int i) {
        return 0;// we do not store arrow data
    }

    @Override
    public int getNextCount(int v) {
        Node node = nodes[v];
        return (node == null) ? 0 : node.next.size();
    }

    @Override
    public int getPrevAsInt(int v, int i) {
        Node node = nodes[v];
        if (node == null) {
            throw new IndexOutOfBoundsException("vidx=" + v + ", i=" + i);
        }
        return node.prev.getAsInt(i);
    }

    @Override
    public int getPrevArrowAsInt(int v, int i) {
        return 0;//we do not store arrow data
    }

    @Override
    public int getPrevCount(int v) {
        Node node = nodes[v];
        return (node == null) ? 0 : node.prev.size();
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Performance: node.next is node.this. This saves 1 object per node.
     */
    private static class Node extends IntArrayList {
        private final IntArrayList next = this;
        private final IntArrayList prev = new IntArrayList();

        private boolean isNodeEmpty() {
            return next.isEmpty() && prev.isEmpty();
        }
    }
}
