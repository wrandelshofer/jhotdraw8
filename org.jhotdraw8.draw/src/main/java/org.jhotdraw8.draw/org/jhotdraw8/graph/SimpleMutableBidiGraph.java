package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.Enumerator;

import java.util.ArrayList;
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
    private List<V> cachedVertices = null;
    private List<A> cachedArrows = null;
    private int arrowCount = 0;

    public SimpleMutableBidiGraph() {
        nodeMap = new LinkedHashMap<>();
    }

    public SimpleMutableBidiGraph(final @NonNull DirectedGraph<V, A> g) {
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
        cachedVertices = null;
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
        cachedVertices = null;
    }

    @Override
    public void addArrow(@NonNull V v, @NonNull V u, @Nullable A a) {
        Node<V, A> vNode = nodeMap.get(v);
        if (vNode == null) {
            throw new IllegalStateException("v=" + v + " is not in graph");
        }
        Node<V, A> uNode = nodeMap.get(u);
        if (uNode == null) {
            throw new IllegalStateException("u=" + u + " is not in graph");
        }
        vNode.next.add(uNode, a);
        uNode.prev.add(vNode, a);
        cachedArrows = null;
        arrowCount++;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u, @Nullable A a) {
        Node<V, A> vNode = nodeMap.get(v);
        if (vNode == null) {
            throw new IllegalStateException("v=" + v + " is not in graph");
        }
        Node<V, A> uNode = nodeMap.get(u);
        if (vNode == null) {
            throw new IllegalStateException("u=" + u + " is not in graph");
        }
        vNode.next.remove(uNode, a);
        uNode.prev.remove(vNode, a);
        cachedArrows = null;
        arrowCount--;
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = nodeMap.get(u);
        vNode.next.remove(uNode);
        uNode.prev.remove(vNode);
        cachedArrows = null;
        arrowCount--;
    }

    @Override
    public void removeArrowAt(@NonNull V v, int k) {
        Node<V, A> vNode = nodeMap.get(v);
        Node<V, A> uNode = vNode.next.getNode(k);
        A uArrow = vNode.next.getArrow(k);
        vNode.next.removeAt(k);
        uNode.prev.remove(vNode, uArrow);
        cachedArrows = null;
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
        if (cachedVertices == null) {
            cachedVertices = new ArrayList<>(nodeMap.keySet());
        }

        return (V) cachedVertices.get(index);
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return nodeMap.keySet();
    }

    @Override
    public @NonNull Collection<A> getArrows() {
        if (cachedArrows == null) {
            Object[] a = new Object[arrowCount];
            int i = 0;
            for (Node<V, A> value : nodeMap.values()) {
                for (Enumerator<A> it = value.next.arrowEnumerator(); it.moveNext(); ) {
                    a[i++] = it.current();
                }
            }
            @SuppressWarnings("unchecked")
            List<A> unchecked = (List<A>) Arrays.asList(a);
            cachedArrows = Collections.unmodifiableList(unchecked);
        }
        return cachedArrows;
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
     * @param <V> the vertex type
     * @param <A> the arrow type
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

        private void rangeCheck(int index, int minInclusive, int maxExclusive) throws IllegalArgumentException {
            if (index < minInclusive || index >= maxExclusive) {
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
            rangeCheck(index, 1, size);
            shrink(size - 1);
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(items, (index + 1) * ITEM_SIZE, items, index * ITEM_SIZE, numMoved * ITEM_SIZE);
            }
            --size;
        }

        private void grow(int targetCapacity) {
            items = grow(targetCapacity, ITEM_SIZE, items);
        }

        private void shrink(int targetCapacity) {
            items = shrink(targetCapacity, ITEM_SIZE, items);
        }

        /**
         * Grows an items array.
         *
         * @param targetCapacity {@literal >= 0}
         * @param itemSize       number of array elements that an item occupies
         * @param items          the items array
         * @return a new item array of larger size or the same if no resizing is necessary
         */
        private static Object[] grow(final int targetCapacity, final int itemSize, @Nullable final Object[] items) {
            if (items == null) {
                return new Object[targetCapacity * itemSize];
            }
            if (targetCapacity * itemSize <= items.length) {
                return items;
            }
            int newCapacity = max(targetCapacity, items.length + items.length / 2); // grow by 50%
            Object[] newItems = new Object[newCapacity * itemSize];
            System.arraycopy(items, 0, newItems, 0, items.length);
            return newItems;
        }

        /**
         * Shrink an items array.
         *
         * @param targetCapacity {@literal >= 0}
         * @param itemSize       number of array elements that an item occupies
         * @param items          the items array
         * @return a new item array of smaller size or the same if no resizing is necessary
         */
        private static Object[] shrink(final int targetCapacity, final int itemSize, @NonNull final Object[] items) {
            if (targetCapacity == 0) {
                return null;
            }
            if (targetCapacity * itemSize < items.length / 8) {
                return items;
            }
            int newCapacity = max(targetCapacity, items.length / 2); // shrink by 50%
            Object[] newItems = new Object[newCapacity * itemSize];
            System.arraycopy(items, 0, newItems, 0, items.length);
            return newItems;
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
