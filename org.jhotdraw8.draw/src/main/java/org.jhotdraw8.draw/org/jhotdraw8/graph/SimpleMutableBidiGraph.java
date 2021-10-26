package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.Enumerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.max;

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
 *    0        Node.next{1,3}
 *    1        Node.next{2,4}.prev{0}
 *    2        Node.prev{1}
 *    3        Node.prev{0}.prev{4}
 *    4        Node.next{3}.prev{1}
 * </pre>
 *
 * @author Werner Randelshofer
 */

public class SimpleMutableBidiGraph<V, A>
        implements MutableBidiGraph<V, A> {
    private final Map<V, Node<V, A>> nodeMap;
    private Object[] vertices = null;
    private List<A> arrows = null;
    private int arrowCount = 0;

    public SimpleMutableBidiGraph() {
        nodeMap = new LinkedHashMap<>();
    }

    public SimpleMutableBidiGraph(DirectedGraph<V, A> g) {
        nodeMap = new LinkedHashMap<>(g.getVertexCount() * 2);
        for (V v : g.getVertices()) {
            addVertex(v);
        }
        for (V v : nodeMap.keySet()) {
            for (Arc<V, A> arc : g.getNextArcs(v)) {
                addArrow(arc.getStart(), arc.getEnd(), arc.getData());
            }
        }
    }

    @Override
    public void addVertex(@NonNull V v) {
        nodeMap.computeIfAbsent(v, Node::new);
        vertices = null;
    }

    @Override
    public void removeVertex(@NonNull V v) {
        Node<V, A> node = nodeMap.remove(v);
        if (node != null) {
            // Unlink node from its "next" nodes
            for (Enumerator<Node<V, A>> i = node.next.nodesEnumerator(); i.moveNext(); ) {
                Node<V, A> next = i.current();
                next.prev.remove(node);
            }
            // Unlink node from its "prev" nodes
            for (Enumerator<Node<V, A>> i = node.prev.nodesEnumerator(); i.moveNext(); ) {
                Node<V, A> prev = i.current();
                prev.next.remove(node);
            }
            // Clear node
            node.next.clear();
            node.prev.clear();
        }
        vertices = null;
    }

    @Override
    public void addArrow(@NonNull V v, @NonNull V u, @Nullable A data) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = nodeMap.get(u);
        vNode.next.add(uNode, data);
        uNode.prev.add(vNode, data);
        arrows = null;
        arrowCount++;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u, @Nullable A data) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = nodeMap.get(u);
        vNode.next.remove(uNode, data);
        uNode.prev.remove(vNode, data);
        arrows = null;
        arrowCount--;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = nodeMap.get(u);
        vNode.next.remove(uNode);
        uNode.prev.remove(vNode);
        arrows = null;
        arrowCount--;
    }

    @Override
    public void removeArrowAt(@NonNull V v, int k) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = vNode.next.getNode(k);
        A uArrow = vNode.next.getArrow(k);
        vNode.next.removeAt(k);
        uNode.prev.remove(vNode, uArrow);
        arrows = null;
        arrowCount--;
    }

    @Override
    public @NonNull V getNext(@NonNull V vertex, int index) {
        Node<V, A> vNode = nodeMap.get(vertex);
        return vNode.next.getVertex(index);
    }

    @Override
    public @NonNull A getNextArrow(@NonNull V vertex, int index) {
        Node<V, A> vNode = nodeMap.get(vertex);
        return vNode.next.getArrow(index);
    }

    @Override
    public int getNextCount(@NonNull V vertex) {
        Node<V, A> vNode = nodeMap.get(vertex);
        return vNode.next.size();
    }

    @Override
    public V getVertex(int index) {
        if (vertices == null) {
            vertices = nodeMap.keySet().toArray(new Object[0]);
        }

        @SuppressWarnings("unchecked")
        V vertex = (V) vertices[index];
        return vertex;
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return nodeMap.keySet();
    }

    @Override
    public @NonNull Collection<A> getArrows() {
        if (arrows == null) {
            Object[] a = new Object[arrowCount];
            int i = 0;
            for (Node<V, A> value : nodeMap.values()) {
                for (Enumerator<A> it = value.next.arrowEnumerator(); it.moveNext(); ) {
                    a[i++] = it.current();
                }
            }
            @SuppressWarnings("unchecked")
            List<A> unchecked = (List<A>) Arrays.asList(a);
            arrows = Collections.unmodifiableList(unchecked);
        }
        return arrows;
    }

    @Override
    public @NonNull V getPrev(@NonNull V vertex, int index) {
        Node<V, A> node = nodeMap.get(vertex);
        return node.prev.getVertex(index);
    }

    @Override
    public @NonNull A getPrevArrow(@NonNull V vertex, int index) {
        Node<V, A> node = nodeMap.get(vertex);
        return node.prev.getArrow(index);
    }

    @Override
    public int getPrevCount(@NonNull V vertex) {
        Node<V, A> node = nodeMap.get(vertex);
        return node.prev.size();
    }


    private static class Node<V, A> {
        @SuppressWarnings("unchecked")
        AdjacentList<V, A> next = new AdjacentList<>();
        @SuppressWarnings("unchecked")
        AdjacentList<V, A> prev = new AdjacentList<>();
        final V vertex;

        public Node(V vertex) {
            this.vertex = vertex;
        }
    }

    /**
     * List of adjacent nodes.
     *
     * @param <V> the vertex type
     * @param <A> the arrow type
     */
    private static class AdjacentList<V, A> {
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
        /**
         * Item array.
         */
        private Object[] items;
        /**
         * Holds the size of the list. Invariant: size >= 0.
         */
        private int size;

        /**
         * Creates a new empty instance with 0 initial capacity.
         */
        public AdjacentList() {
        }

        /**
         * Adds a new item to the end of the list.
         *
         * @param node  the node
         * @param arrow the arrow data
         */
        public void add(Node<V, A> node, A arrow) {
            increaseCapacity(size + 1);
            int index = size++;
            items[index * ITEM_SIZE + ITEM_NODE_OFFSET] = node;
            items[index * ITEM_SIZE + ITEM_ARROW_OFFSET] = arrow;
        }

        /**
         * Gets the node at the specified index.
         *
         * @param index an index
         * @return the node at the index
         */
        @SuppressWarnings("unchecked")
        public Node<V, A> getNode(int index) {
            rangeCheck(index, size);
            return (Node<V, A>) items[index * ITEM_SIZE + ITEM_NODE_OFFSET];
        }

        @SuppressWarnings("unchecked")
        public V getVertex(int index) {
            rangeCheck(index, size);
            return ((Node<V, A>) items[index * ITEM_SIZE + ITEM_NODE_OFFSET]).vertex;
        }

        /**
         * Gets the arrow data at the specified index.
         *
         * @param index an index
         * @return the node at the index
         */
        @SuppressWarnings("unchecked")
        public A getArrow(int index) {
            rangeCheck(index, size);
            return (A) items[index * ITEM_SIZE + ITEM_ARROW_OFFSET];
        }

        private void increaseCapacity(int capacity) {
            if (items == null) {
                items = new Object[capacity * ITEM_SIZE];
            }
            if (capacity * ITEM_SIZE <= items.length) {
                return;
            }
            int newCapacity = max(capacity, size + size / 2); // grow by 50%
            Object[] newItems = new Object[newCapacity * ITEM_SIZE];
            System.arraycopy(items, 0, newItems, 0, size * ITEM_SIZE);
            items = newItems;
        }

        private Enumerator<Node<V, A>> nodesEnumerator() {
            return new AbstractEnumeratorSpliterator<Node<V, A>>(size, 0) {
                int index = 0;

                @Override
                public boolean moveNext() {
                    if (index < size) {
                        current = getNode(index++);
                    }
                    return false;
                }
            };
        }

        private Enumerator<A> arrowEnumerator() {
            return new AbstractEnumeratorSpliterator<A>(size, 0) {
                int index = 0;

                @Override
                public boolean moveNext() {
                    if (index < size) {
                        current = getArrow(index++);
                    }
                    return false;
                }
            };
        }

        /**
         * Returns true if size==0.
         *
         * @return true if empty
         */
        public boolean isEmpty() {
            return size == 0;
        }

        private void rangeCheck(int index, int maxExclusive) throws IllegalArgumentException {
            if (index < 0 || index >= maxExclusive) {
                throw new IndexOutOfBoundsException("Index out of bounds " + index);
            }
        }

        public void remove(Node<V, A> n) {
            for (int i = 0; i < size; i++) {
                if (getNode(i).equals(n)) {
                    removeAt(i);
                    return;
                }
            }
        }

        public void remove(Node<V, A> n, A data) {
            for (int i = 0; i < size; i++) {
                if (getNode(i).equals(n) && getArrow(i).equals(data)) {
                    removeAt(i);
                    return;
                }
            }
        }

        /**
         * Removes the item at the specified index from this list.
         *
         * @param index an index
         * @return the removed item
         */
        public void removeAt(int index) {
            rangeCheck(index, size);
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(items, (index + 1) * ITEM_SIZE, items, index * ITEM_SIZE, numMoved * ITEM_SIZE);
            }
            --size;
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
