package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.vector.GraphvizBitMappedTrie;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistentVectorListBitMappedTrieTest {
    @Test
    public void shouldAppend() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        actual.addAll(expected);

        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingFirst() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        actual.removeFirst();
        expected.removeFirst();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingFirstThenAddingFirst() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        actual.removeFirst();
        actual.addFirst(-1);
        expected.removeFirst();
        expected.addFirst(-1);
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingFirstThenAddingLast() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        actual.removeFirst();
        actual.addLast(-1);
        expected.removeFirst();
        expected.addLast(-1);
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingLast() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        actual.removeLast();
        expected.removeLast();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingRangeAtBeginning() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        actual.removeRange(0, 1_000);
        expected.subList(0, 1_000).clear();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingRangeFirstOneByOne() {
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        for (int i = 0; i < 1000; i++) {
            actual.removeFirst();
        }
        expected.subList(0, 1_000).clear();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingOneByOne() {
        // Performance: This is fast because we remove the first element in each iteration
        var actual = new MutableVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());
        actual.addAll(expected);
        for (var e : expected) {
            actual.remove(e);
        }
        expected.subList(0, 1_000).clear();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldRemovingOneByOneImmutableShuffled() {
        // Performance: This is quadratic and will take forever if the array is larger than a few thousand
        var actual = new PersistentVectorList<Integer>();
        List<Integer> expected = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        actual = actual.addingAll(expected);
        List<Integer> expectedShuffled = new ArrayList<>(expected);
        Collections.shuffle(expectedShuffled);
        for (var e : expectedShuffled) {
            actual = actual.removing(e);
        }
        expected.clear();
        IO.println(new GraphvizBitMappedTrie().toGraphviz(actual.root));
        assertEquals(expected, actual.asList());
    }
}
