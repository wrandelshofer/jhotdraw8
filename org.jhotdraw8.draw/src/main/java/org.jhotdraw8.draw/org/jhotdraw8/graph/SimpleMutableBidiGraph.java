/*
 * @(#)SimpleMutableBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractEnumerator;
import org.jhotdraw8.collection.Enumerator;
import org.jhotdraw8.collection.ListHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A mutable bidi graph with balanced performance for all operations.
 * <ul>
 *     <li>Insertion of a vertex is done in amortized {@code O(1)}.</li>
 *     <li>Insertion of an arrow is done in amortized {@code O(1)}.</li>
 *     <li>Removal of a vertex is done in amortized {@code O(|A'|)},
 *     where {@code |A'|} is the number of ingoing and outgoing arrows of the vertex.</li>
 *     <li>Removal of an arrow is done in amortized {@code O(|A'|)},
 *      where {@code |A'|} is the number of ingoing and outgoing arrows of the
 *      involved vertices.</li>
 * </ul>
 * Memory locality is poor. If you need to perform query operations on the
 * graph, then an immutable graph will give you better performance.
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
 *     addVertex(0);
 *     addVertex(1);
 *     addVertex(2);
 *     addVertex(3);
 *     addVertex(4);
 *     addArrow(0, 1);
 *     addArrow(0, 3);
 *     addArrow(1, 2);
 *     addArrow(1, 4);
 *     addArrow(4, 3);
 * </pre>
 * Then the internal representation is as follows:
 * <pre>
 * vertex#  next- and prev-arrows
 *
 *    0:    next={1, 3};  prev={}
 *    1:    next={2, 4};  prev={0}
 *    2:    next={};      prev={1}
 *    3:    next={};      prev={0, 4}
 *    4:    next={3};     prev={1}
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class SimpleMutableBidiGraph<V, A> implements MutableBidiGraph<V, A> {

    private final Map<V, Node<V, A>> nodeMap;
    private List<V> cachedVertices = null;
    private int arrowCount = 0;

    /**
     * Creates a new instance.
     */
    public SimpleMutableBidiGraph() {
        this(10, 10);
    }

    /**
     * Creates a new instance.
     *
     * @param initialVertexCapacity the initial vertex capacity
     * @param initialArrowCapacity  the initial arrow capacity (ignored)
     */
    public SimpleMutableBidiGraph(final int initialVertexCapacity, final int initialArrowCapacity) {
        nodeMap = new LinkedHashMap<>(initialVertexCapacity * 2);
    }

    /**
     * Creates a new instance with a copy of the provided graph
     *
     * @param g a graph
     */
    public SimpleMutableBidiGraph(final @NonNull DirectedGraph<V, A> g) {
        nodeMap = new LinkedHashMap<>(g.getVertexCount() * 2);
        for (V v : g.getVertices()) {
            addVertex(v);
        }
        for (V v : nodeMap.keySet()) {
            for (Arc<V, A> arc : g.getNextArcs(v)) {
                addArrow(arc.getStart(), arc.getEnd(), arc.getArrow());
            }
        }
    }

    @Override
    public void addVertex(@NonNull V v) {
        if (nodeMap.containsKey(v)) {
            return;
        }
        nodeMap.put(v, new Node<>(v));
        cachedVertices = null;
    }

    @Override
    public void removeVertex(@NonNull V v) {
        Node<V, A> node = nodeMap.remove(v);
        if (node == null) {
            return;
        }
        int oldArrowCount = arrowCount;
        // Unlink node from its "next" nodes
        for (Enumerator<Node<V, A>> i = node.next.nodesEnumerator(); i.moveNext(); ) {
            Node<V, A> next = i.current();
            next.prev.remove(node);
            arrowCount--;
        }
        // Unlink node from its "prev" nodes
        for (Enumerator<Node<V, A>> i = node.prev.nodesEnumerator(); i.moveNext(); ) {
            Node<V, A> prev = i.current();
            prev.next.remove(node);
            arrowCount--;
        }
        // Clear node
        node.next.clear();
        node.prev.clear();

        cachedVertices = null;
    }

    @Override
    public void addArrow(@NonNull V v, @NonNull V u, @Nullable A a) {
        Node<V, A> vNode = getNodeNonNull(v);
        Node<V, A> uNode = getNodeNonNull(u);
        vNode.next.add(uNode, a);
        uNode.prev.add(vNode, a);
        arrowCount++;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u, @Nullable A a) {
        Node<V, A> vNode = getNodeNonNull(v);
        Node<V, A> uNode = getNodeNonNull(u);
        if (!vNode.next.remove(uNode, a)) {
            throw new IllegalStateException("arrow v=" + v + " u=" + u + " a=" + a + " is not in graph");
        }
        if (!uNode.prev.remove(vNode, a)) {
            throw new IllegalStateException("arrow v=" + v + " u=" + u + " a=" + a + " is not in graph");
        }
        arrowCount--;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u) {
        Node<V, A> vNode = getNodeNonNull(v);
        Node<V, A> uNode = getNodeNonNull(u);
        if (!vNode.next.remove(uNode)) {
            throw new IllegalStateException("arrow v=" + v + " u=" + u + " is not in graph");
        }
        if (!uNode.prev.remove(vNode)) {
            throw new IllegalStateException("arrow v=" + v + " u=" + u + " is not in graph");
        }
        arrowCount--;
    }

    private @NonNull Node<V, A> getNodeNonNull(@NonNull V v) {
        final Node<V, A> node = nodeMap.get(v);
        if (node == null) {
            throw new IllegalArgumentException("vertex " + v + " is not in graph");
        }
        return node;
    }

    @Override
    public void removeNext(@NonNull V v, int k) {
        Node<V, A> vNode = getNodeNonNull(v);
        Node<V, A> uNode = vNode.next.getNode(k);
        A uArrow = vNode.next.getArrow(k);
        vNode.next.removeAt(k);
        if (!uNode.prev.remove(vNode, uArrow)) {
            throw new IllegalStateException("arrow v=" + v + " k=" + k + " is not in graph");
        }
        arrowCount--;
    }

    @Override
    public @NonNull V getNext(@NonNull V v, int index) {
        Node<V, A> vNode = getNodeNonNull(v);
        return vNode.next.getVertex(index);
    }

    @Override
    public @Nullable A getNextArrow(@NonNull V v, int index) {
        Node<V, A> vNode = getNodeNonNull(v);
        return vNode.next.getArrow(index);
    }

    @Override
    public int getNextCount(@NonNull V v) {
        Node<V, A> vNode = getNodeNonNull(v);
        return vNode.next.size();
    }

    @Override
    public V getVertex(int index) {
        if (cachedVertices == null) {
            cachedVertices = Collections.unmodifiableList(new ArrayList<>(nodeMap.keySet()));
        }

        return cachedVertices.get(index);
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return Collections.unmodifiableSet(nodeMap.keySet());
    }

    @Override
    public int getVertexCount() {
        return nodeMap.size();
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public @NonNull V getPrev(@NonNull V vertex, int index) {
        Node<V, A> node = getNodeNonNull(vertex);
        return node.prev.getVertex(index);
    }

    @Override
    public @NonNull A getPrevArrow(@NonNull V vertex, int index) {
        Node<V, A> node = getNodeNonNull(vertex);
        return node.prev.getArrow(index);
    }

    @Override
    public int getPrevCount(@NonNull V vertex) {
        Node<V, A> node = getNodeNonNull(vertex);
        return node.prev.size();
    }


    /**
     * A Node holds a vertex and an adjacency list
     * for the next nodes and an adjacency list for
     * the previous nodes.
     *
     * @param <V>
     * @param <A>
     */
    private static class Node<V, A> {
        final AdjacencyList<V, A> next = new AdjacencyList<>();
        final AdjacencyList<V, A> prev = new AdjacencyList<>();
        final V vertex;

        public Node(V vertex) {
            this.vertex = vertex;
        }
    }

    /**
     * List of adjacent nodes, each element is a tuple {@literal (Node<V,A>,A)}.
     *
     * @param <V> the vertex data type
     * @param <A> the arrow data type
     */
    private static class AdjacencyList<V, A> {
        /**
         * An item occupies {@value #ITEM_SIZE} array entries.
         */
        private final static int ITEM_SIZE = 2;
        /**
         * An array entry with offset {@value #ITEM_NODE_OFFSET}
         * contains a {@link Node}.
         */
        private final static int ITEM_NODE_OFFSET = 0;
        /**
         * An array entry with offset {@value #ITEM_ARROW_OFFSET}
         * contains a {@code A}.
         */
        private final static int ITEM_ARROW_OFFSET = 1;
        private final static @NonNull Object[] EMPTY_ARRAY = new Object[0];
        /**
         * Item array.
         */
        private @NonNull Object[] items = EMPTY_ARRAY;
        /**
         * Holds the size of the list. Invariant: size >= 0.
         */
        private int size;

        /**
         * Creates a new empty instance with 0 initial capacity.
         */
        public AdjacencyList() {
        }

        /**
         * Adds a new item to the end of the list.
         *
         * @param node the node
         * @param a    the arrow data
         */
        public void add(Node<V, A> node, A a) {
            grow(size + 1);
            int index = size++;
            items[index * ITEM_SIZE + ITEM_NODE_OFFSET] = node;
            items[index * ITEM_SIZE + ITEM_ARROW_OFFSET] = a;
        }

        /**
         * Gets the node at the specified index.
         *
         * @param index an index
         * @return the node at the index
         */
        public Node<V, A> getNode(int index) {
            rangeCheck(index, size);

            @SuppressWarnings("unchecked")
            Node<V, A> unchecked = (Node<V, A>) items[index * ITEM_SIZE + ITEM_NODE_OFFSET];
            return unchecked;
        }

        public V getVertex(int index) {
            return getNode(index).vertex;
        }

        /**
         * Gets the arrow data at the specified index.
         *
         * @param index an index
         * @return the node at the index
         */
        public A getArrow(int index) {
            rangeCheck(index, size);

            @SuppressWarnings("unchecked")
            A unchecked = (A) items[index * ITEM_SIZE + ITEM_ARROW_OFFSET];
            return unchecked;
        }

        private Enumerator<Node<V, A>> nodesEnumerator() {
            // We must use explicit type arguments in Java 8!
            return new AbstractEnumerator<Node<V, A>>(size, 0) {
                int index = 0;

                @Override
                public boolean moveNext() {
                    if (index < size) {
                        current = getNode(index++);
                        return true;
                    }
                    return false;
                }
            };
        }

        private void rangeCheck(int index, int maxExclusive) throws IllegalArgumentException {
            if (index < 0 || index >= maxExclusive) {
                throw new IndexOutOfBoundsException("Index out of bounds " + index);
            }
        }

        public boolean remove(Node<V, A> n) {
            for (int i = 0; i < size; i++) {
                if (getNode(i).equals(n)) {
                    removeAt(i);
                    return true;
                }
            }
            return false;
        }

        public boolean remove(Node<V, A> n, A a) {
            for (int i = 0; i < size; i++) {
                if (getNode(i).equals(n)
                        && getArrow(i).equals(a)) {
                    removeAt(i);
                    return true;
                }
            }
            return false;
        }

        /**
         * Removes the item at the specified index from this list.
         *
         * @param index an index
         */
        public void removeAt(int index) {
            rangeCheck(index, size);
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(items, (index + 1) * ITEM_SIZE, items, index * ITEM_SIZE, numMoved * ITEM_SIZE);
            }
            --size;
        }

        private void grow(int targetCapacity) {
            items = ListHelper.grow(size, targetCapacity, ITEM_SIZE, items);
        }

        /**
         * Returns the size of the list.
         *
         * @return the size
         */
        public int size() {
            return size;
        }

        public void clear() {
            Arrays.fill(items, 0, size, null);
            size = 0;
        }
    }
}
