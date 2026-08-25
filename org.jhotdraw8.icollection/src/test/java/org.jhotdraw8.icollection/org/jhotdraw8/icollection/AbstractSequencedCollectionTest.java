/*
 * @(#)AbstractIntSequencedCollectionTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import de.sandec.jmemorybuddy.JMemoryBuddy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests classes that implement the interface [SequencedCollection].
public abstract class AbstractSequencedCollectionTest {
    private static int size = 17;
    private static int bound = (int) Math.min(Integer.MAX_VALUE, size * 1000L);
    private static final SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, size, bound);
    private static final SetData NO_COLLISION = SetData.newData("no collisions", -1, size, bound);
    private static final SetData ALL_COLLISION = SetData.newData("all collisions", 0, size, bound);
    private static final SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, size, bound);

    public static Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    public AbstractSequencedCollectionTest() {
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstShouldMakeElementCollectable(SetData data) throws Exception {
        JMemoryBuddy.memoryTest(checker -> {
            var a = data.a.stream().map(Key::clone).toList();
            var b = data.b.stream().map(Key::clone).toList();

            var instance = newInstance();
            instance.addAll(a);
            var removed = instance.removeFirst();

            checker.setAsReferenced(instance);

            checker.assertCollectable(removed); // notReferenced should be collectable
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldMakeElementCollectable(SetData data) throws Exception {
        JMemoryBuddy.memoryTest(checker -> {
            var a = data.a.stream().map(Key::clone).toList();
            var b = data.b.stream().map(Key::clone).toList();

            var instance = newInstance();
            instance.addAll(a);
            var removed = instance.removeFirst();

            checker.setAsReferenced(instance);

            checker.assertCollectable(removed); // notReferenced should be collectable
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldAddFirst(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());
        instance.addFirst(data.a.iterator().next());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());
        expected.add(0, data.a.iterator().next());

        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldRemoveFirst(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());

        while (!expected.isEmpty()) {
            instance.removeFirst();
            expected.removeFirst();
            assertEquals(expected, instance);
        }

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldRemoveLast(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());

        while (!expected.isEmpty()) {
            instance.removeLast();
            expected.removeLast();
            assertEquals(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldIterateOverElementsInSequence(SetData data) {
        var instance = newInstance();
        instance.addAll(data.a().asSet());
        Iterator<Key> actual = instance.iterator();
        Iterator<Key> expected = data.a().iterator();
        assertEquals(actual.hasNext(), expected.hasNext());
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            Key actualKey = actual.next();
            Key expectedKey = expected.next();
            assertEquals(expectedKey, actualKey);
        }
        assertFalse(actual.hasNext());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldReverseIterateOverElementsInSequence(SetData data) {
        var instance = newInstance();
        instance.addAll(data.a().asSet());
        Iterator<Key> actual = instance.reversed().iterator();
        Iterator<Key> expected = new ArrayList<>(data.a().asSet()).reversed().iterator();
        assertEquals(actual.hasNext(), expected.hasNext());
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            Key actualKey = actual.next();
            Key expectedKey = expected.next();
            assertEquals(expectedKey, actualKey);
        }
        assertFalse(actual.hasNext());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldRemoveElement(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());
        Random rng = new Random(0);
        while (!expected.isEmpty()) {
            Key e = expected.get(rng.nextInt(expected.size()));
            int expectedIndex = expected.indexOf(e);
            boolean removed = instance.remove(e);
            expected.remove(e);
            assertTrue(removed, "element should have been removed " + e + " expected index= " + expectedIndex);
            assertEquals(expected, instance);
        }
    }

    protected abstract SequencedCollection<Key> newInstance();

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldAddLast(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());
        instance.addLast(data.a.iterator().next());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());
        expected.add(data.a.iterator().next());

        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldAddAll(SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.a.asCollection());
        instance.addAll(data.b.asCollection());
        instance.addAll(data.c.asCollection());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.a.asCollection());
        expected.addAll(data.b.asCollection());
        expected.addAll(data.c.asCollection());

        assertEquals(expected, instance);
    }

    @Test
    public void spliteratorShouldSupportEncounterOrder() throws Exception {
        SequencedCollection<Key> instance = newInstance();
        assertEquals(instance.spliterator().characteristics() & Spliterator.ORDERED, Spliterator.ORDERED, "spliterator should be ordered");
        assertEquals(instance.reversed().spliterator().characteristics() & Spliterator.ORDERED, Spliterator.ORDERED, "spliterator should be ordered");
    }
}
