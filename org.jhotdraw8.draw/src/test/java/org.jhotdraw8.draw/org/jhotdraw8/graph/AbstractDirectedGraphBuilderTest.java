package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractDirectedGraphBuilderTest {
    @Test
    public void testAddAndIterate() {
        DirectedGraphBuilder<String, Integer> instance = createInstance();
        for (String s : List.of("a", "a0", "a1", "a2", "a3", "a4",
                "b", "b10", "b11", "b12", "b13", "b14")) {
            instance.addVertex(s);

        }

        instance.addArrow("a", "a0", 0);
        instance.addArrow("a", "a1", 1);
        instance.addArrow("a", "a2", 2);
        instance.addArrow("a", "a3", 3);
        instance.addArrow("a", "a4", 4);
        instance.addArrow("b", "b10", 10);
        instance.addArrow("b", "b11", 11);
        instance.addArrow("b", "b12", 12);
        instance.addArrow("b", "b13", 13);
        instance.addArrow("b", "b14", 14);

        assertEquals(5, instance.getNextCount("a"));
        assertEquals(5, instance.getNextCount("b"));
        assertEquals(0, instance.getNextCount("b10"));

        // test: getNextCount() and getNext() for "a"
        List<String> expected = new ArrayList<>(List.of("a0", "a1", "a2", "a3", "a4"));
        List<String> actual = new ArrayList<>();
        for (int i = 0, n = instance.getNextCount("a"); i < n; i++) {
            actual.add(instance.getNext("a", i));
        }
        assertEquals(expected, actual);

        // test: getNextVertices() for "a"
        actual.clear();
        actual.addAll(instance.getNextVertices("a"));
        assertEquals(expected, actual);

        // test: getNextCount() and getNext() for "b10"
        expected.clear();
        actual.clear();
        for (int i = 0, n = instance.getNextCount("b10"); i < n; i++) {
            actual.add(instance.getNext("b10", i));
        }
        assertEquals(expected, actual);

        // test: getNextVertices() for "b10"
        actual.clear();
        actual.addAll(instance.getNextVertices("b10"));
        assertEquals(expected, actual);

    }

    @NonNull
    abstract protected DirectedGraphBuilder<String, Integer> createInstance();
}