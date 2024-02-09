/*
 * @(#)DepthFirstArcSpliteratorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.iterator.BfsDfsArcSpliterator;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class BfsDfsArcSpliteratorTest {
    protected @NonNull DirectedGraph<Integer, Integer> createDoubleVertexGraph() {
        final SimpleMutableDirectedGraph<Integer, Integer> builder = new SimpleMutableDirectedGraph<>();

        //  (1)->(2)------------->(3)------->(4)----
        //      ↗︎                         ↗︎         \
        //     /                         /           ↘
        //  (5)------->(6)------->(7)----            (9)
        //                \                         ↗︎
        //                  ↘                     /
        //                   (8)-----------------


        for (int i = 1; i < 10; i++) {
            builder.addVertex(i);
            builder.addVertex(i * 10);
        }
        builder.addArrow(1, 2, 100);
        builder.addArrow(2, 3, 100);
        builder.addArrow(3, 4, 100);
        builder.addArrow(4, 9, 100);
        builder.addArrow(5, 2, 100);
        builder.addArrow(5, 6, 100);
        builder.addArrow(6, 7, 100);
        builder.addArrow(6, 8, 100);
        builder.addArrow(7, 4, 100);
        builder.addArrow(8, 9, 100);
        return new ImmutableAttributed32BitIndexedDirectedGraph<Integer, Integer>((DirectedGraph<Integer, Integer>) builder);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsIterator() {
        return Arrays.asList(
                dynamicTest("1", () -> testIterator(createDoubleVertexGraph(), Arrays.asList(5, 9),
                        "5->6:100,6->8:100,8->9:100,6->7:100,7->4:100,4->9:100,5->2:100,2->3:100,3->4:100"))
        );
    }

    private void testIterator(DirectedGraph<Integer, Integer> graph, List<Integer> waypoints, String expected) {
        Set<Integer> goals = new HashSet<>(waypoints);
        StringBuilder buf = new StringBuilder();
        for (Integer root : waypoints) {
            BfsDfsArcSpliterator<Integer, Integer> itr = new BfsDfsArcSpliterator<>(graph::getNextArcs, root, true);
            while (itr.moveNext()) {
                Arc<Integer, Integer> current = itr.current();
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(current.getStart() + "->" + current.getEnd() + ":" + current.getArrow());
            }
        }
        String actual = buf.toString();
        assertEquals(expected, actual);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsPathBuilding() {
        return Arrays.asList(
                dynamicTest("5->", () -> testPathBuilding(createDoubleVertexGraph(), Arrays.asList(5),
                        "[[5, 6, 8, 9], [6, 7, 4, 9], [5, 2, 3, 4]]")),
                dynamicTest("1->", () -> testPathBuilding(createDoubleVertexGraph(), Arrays.asList(1),
                        "[[1, 2, 3, 4, 9]]"))
        );
    }

    private void testPathBuilding(DirectedGraph<Integer, Integer> graph, List<Integer> waypoints,
                                  String expected) {
        List<ImmutableList<Integer>> paths = new ArrayList<>();
        List<Integer> path = null;
        for (Integer root : waypoints) {
            BfsDfsArcSpliterator<Integer, Integer> itr = new BfsDfsArcSpliterator<>(graph::getNextArcs, root, true);
            while (itr.moveNext()) {
                Arc<Integer, Integer> current = itr.current();
                if (path == null) {
                    path = new ArrayList<>();
                    path.add(current.getStart());
                    path.add(current.getEnd());
                } else if (path.get(path.size() - 1).equals(current.getStart())) {
                    path.add(current.getEnd());
                } else {
                    paths.add(VectorList.copyOf(path));
                    path = new ArrayList<>();
                    path.add(current.getStart());
                    path.add(current.getEnd());
                }
            }
        }
        if (path != null) {
            paths.add(VectorList.copyOf(path));
        }

        String actual = paths.toString();
        assertEquals(expected, actual);
    }
}
