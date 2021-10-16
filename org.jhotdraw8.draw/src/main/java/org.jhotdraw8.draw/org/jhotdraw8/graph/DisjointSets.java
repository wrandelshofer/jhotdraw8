package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DisjointSets {
    /**
     * Don't let anyone instantiate this class.
     */
    private DisjointSets() {

    }

    /**
     * Given a directed graph, returns all disjoint sets of vertices.
     * <p>
     * Uses Kruskal's algorithm.
     *
     * @param <V>   the vertex type
     * @param <A>   the arrow type
     * @param graph a directed graph
     * @return the disjoint sets.
     */
    public static @NonNull <V, A> List<Set<V>> findDisjointSets(@NonNull DirectedGraph<V, A> graph) {
        return findDisjointSets(graph.getVertices(), graph::getNextVertices);
    }

    /**
     * Given a directed graph, returns all disjoint sets of vertices.
     * <p>
     * Uses Kruskal's algorithm.
     *
     * @param <V>             the vertex type
     * @param vertices        the vertices of the directed graph
     * @param getNextVertices a function that returns the next vertices given a vertex
     * @return the disjoint sets.
     */
    public static @NonNull <V> List<Set<V>> findDisjointSets(@NonNull Collection<V> vertices, @NonNull Function<V, Iterable<V>> getNextVertices) {
        // Create initial forest
        Map<V, List<V>> forest = MinimumSpanningTree.createForest(vertices);
        // Merge sets.
        for (V u : vertices) {
            for (V v : getNextVertices.apply(u)) {
                List<V> uset = forest.get(u);
                List<V> vset = forest.get(v);
                if (uset != vset) {
                    MinimumSpanningTree.union(uset, vset, forest);
                }
            }
        }

        // Create final forest.
        Set<List<V>> visited = Collections.newSetFromMap(new IdentityHashMap<>(forest.size()));
        List<Set<V>> disjointSets = new ArrayList<>(forest.size());
        for (List<V> set : forest.values()) {
            if (visited.add(set)) {
                disjointSets.add(new LinkedHashSet<>(set));
            }
        }
        return disjointSets;
    }
}
