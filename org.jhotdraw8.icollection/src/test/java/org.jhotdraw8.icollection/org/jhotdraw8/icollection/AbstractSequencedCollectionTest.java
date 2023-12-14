/*
 * @(#)AbstractIntSequencedCollectionTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.sequenced.SequencedCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests classes that implement the interface {@link SequencedCollection< Key >}.
 */

public abstract class AbstractSequencedCollectionTest {
    private static final SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, 32, 100_000);
    private static final SetData NO_COLLISION = SetData.newData("no collisions", -1, 32, 100_000);
    private static final SetData ALL_COLLISION = SetData.newData("all collisions", 0, 32, 100_000);
    private static final SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, 32, 100_000);

    public static @NonNull Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    public AbstractSequencedCollectionTest() {
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldAddFirst(@NonNull SetData data) throws Exception {
        SequencedCollection<Key> instance = newInstance();
        instance.addAll(data.b.asCollection());
        instance.addFirst(data.a.iterator().next());

        List<Key> expected = new ArrayList<>();
        expected.addAll(data.b.asCollection());
        expected.add(0, data.a.iterator().next());

        assertEquals(expected, instance);
    }

    @NonNull
    protected abstract SequencedCollection<Key> newInstance();

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldAddLast(@NonNull SetData data) throws Exception {
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
    public void shouldAddAll(@NonNull SetData data) throws Exception {
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
        assertEquals(instance._reversed().spliterator().characteristics() & Spliterator.ORDERED, Spliterator.ORDERED, "spliterator should be ordered");
    }
}
