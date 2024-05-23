package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.immutable.ImmutableSortedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractImmutableSetTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <E> ImmutableSet<E> newInstance();


    protected abstract <E> Set<E> toMutableInstance(ImmutableSet<E> m);

    protected abstract <E> ImmutableSet<E> toImmutableInstance(Set<E> m);

    protected abstract <E> ImmutableSet<E> toClonedInstance(ImmutableSet<E> m);

    protected abstract boolean supportsNullKeys();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> ImmutableSet<E> newInstance(Iterable<E> m);

    public static Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private static final SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, 32, 100_000);
    private static final SetData NO_COLLISION = SetData.newData("no collisions", -1, 32, 100_000);
    private static final SetData ALL_COLLISION = SetData.newData("all collisions", 0, 32, 100_000);
    private static final SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, 32, 100_000);

    private static int createNewValue(Random rng, Set<Integer> usedValues, int bound) {
        int value;
        int count = 0;
        do {
            value = rng.nextInt(bound);
            count++;
            if (count >= bound) {
                throw new RuntimeException("error in rng");
            }
        } while (!usedValues.add(value));
        return value;
    }


    protected void assertEqualSet(ReadOnlySet<Key> expected, ImmutableSet<Key> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(Set<Key> expected, ImmutableSet<Key> actual) {
        ArrayList<Key> expectedValues = new ArrayList<>(expected);
        ArrayList<Key> actualValues = new ArrayList<>(actual.asSet());
        expectedValues.sort(Comparator.comparing(Key::getValue));
        actualValues.sort(Comparator.comparing(Key::getValue));
        assertEquals(expectedValues, actualValues);

        for (var e : expected) {
            assertTrue(actual.contains(e), "must contain " + e);
        }

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asSet());
        assertEquals(actual.asSet(), expected);
    }

    protected void assertNotEqualSet(Set<Key> expected, ImmutableSet<Key> actual) {
        assertNotEquals(expected, actual.asSet());
        assertNotEquals(actual.asSet(), expected);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        ImmutableSet<Key> actual = newInstance();
        LinkedHashSet<Key> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsShouldBeEqualToSet(SetData data) {
        ImmutableSet<Key> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsOfSameTypeShouldBeEqualToSet(SetData data) {
        ImmutableSet<Key> actual1 = newInstance(data.a().asSet());
        ImmutableSet<Key> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgShouldBeEqualToArg(SetData data) {
        ImmutableSet<Key> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgWithThisShouldBeEqualToArg(SetData data) {
        ImmutableSet<Key> actual = newInstance(data.a());
        ImmutableSet<Key> actual2 = newInstance(actual);
        assertEqualSet(data.a().asSet(), actual2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgsWithMutableSetArgsOfSameTypeShouldBeEqualToSet(SetData data) {
        ImmutableSet<Key> actual1 = newInstance(data.a());
        ImmutableSet<Key> actual = newInstance(toMutableInstance(actual1));
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void emptyShouldYieldEmptySet(SetData data) {
        ImmutableSet<Key> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        ImmutableSet<Key> actual2 = actual.empty();
        assertNotSame(actual, actual2);
        assertEqualSet(Collections.emptySet(), actual2);
    }



    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualSet(SetData data) {
        //   io.vavr.collection.TreeSet<Key> vavrSet=    io.vavr.collection.TreeSet.empty();
        //  vavrSet= vavrSet.addAll(data.a());
        //  System.out.println("vavrSet="+vavrSet);


        ImmutableSet<Key> instance = newInstance(data.a());
        ImmutableSet<Key> clone = toClonedInstance(instance);
        assertEqualSet(data.a().asSet(), clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsShouldYieldExpectedValue(SetData data) {
        ImmutableSet<Key> instance = newInstance(data.a());
        for (Key k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (Key k : data.c()) {
            assertFalse(instance.contains(k));
        }
        try {
            boolean contains = instance.contains(new Object());
            assertFalse(contains);
        } catch (ClassCastException e) {
            assertInstanceOf(ImmutableSortedSet.class, instance, "Only an ImmutableSortedSet may throw a ClassCastException");
        }
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowUnsupportedOperationException(SetData data) {
        ImmutableSet<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> toRemove = new ArrayList<>(new HashSet<>(data.a().asSet()));
        outer:
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<Key> i = instance.iterator(); i.hasNext(); ) {
                Key k = i.next();
                if (k.equals(toRemove.get(0))) {
                    assertThrows(UnsupportedOperationException.class, i::remove);
                    toRemove.remove(0);
                    assertEqualSet(expected, instance);
                    continue outer;
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowException(SetData data) {
        ImmutableSet<Key> instance = newInstance(data.a());
        Iterator<Key> i = instance.iterator();
        assertThrows(Exception.class, i::remove);
        Iterator<Key> k = instance.iterator();
        assertThrows(Exception.class, k::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameSet(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a());
        if (instance instanceof Serializable) {
            assertEqualSet(data.a().asSet(), instance);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            ImmutableSet<Key> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (ImmutableSet<Key>) in.readObject();
            }
            assertEqualSet(data.a().asSet(), deserialized);
        }
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableAssertion"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(SetData data) {
        ImmutableSet<Key> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneShouldYieldTrue(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a());
        ImmutableSet<Key> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone.asSet());
        assertEquals(instance, clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addNullContainsNullShouldReturnTrue(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance();
        var expected = new LinkedHashSet<Key>();
        expected.add(null);
        var actual = instance.add(null);
        assertFalse(instance.contains(null));
        assertTrue(actual.contains(null));
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllNullContainsNullShouldReturnTrue(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance();
        var expected = new LinkedHashSet<Key>();
        expected.addAll(Collections.singleton(null));
        var actual = instance.addAll(Collections.singleton(null));
        assertFalse(instance.contains(null));
        assertTrue(actual.contains(null));
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithNewElementShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        for (Key e : data.c) {
            ImmutableSet<Key> instance2 = instance.add(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            expected.add(e);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithContainedElementShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        for (Key e : data.a) {
            ImmutableSet<Key> instance2 = instance.add(e);
            assertSame(instance, instance2);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithNewElementsShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.addAll(data.c);
        assertNotSame(instance, instance2);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithContainedElementsShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.addAll(data.a.asSet());
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSelfShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.addAll(instance);
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithCloneShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> clone = toClonedInstance(instance);
        assertNotSame(instance, clone);
        ImmutableSet<Key> instance3 = instance.addAll(clone);
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithCloneToMutableShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = toClonedInstance(instance);
        ImmutableSet<Key> instance3 = instance.addAll(instance2.toMutable());
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithNewElementShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        for (Key e : data.c) {
            ImmutableSet<Key> instance2 = instance.remove(e);
            assertSame(instance, instance2);
            assertEqualSet(data.a, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithContainedKeyShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        for (Key e : data.a) {
            expected.remove(e);
            ImmutableSet<Key> instance2 = instance.remove(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithNewKeyShouldReturnSameInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.removeAll(data.c.asSet());
        assertSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithContainedKeyShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.removeAll(data.a.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithCloneShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> clone = toClonedInstance(instance);
        assertNotSame(instance, clone);
        ImmutableSet<Key> instance2 = instance.removeAll(clone);
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithReadOnlySetWithSomeContainedKeyShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.removeAll(data.someAPlusSomeB);
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithSetWithSomeContainedKeyShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.removeAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithSameTypeWithSomeContainedKeyShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.removeAll(newInstance(data.someAPlusSomeB));
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.removeAll(data.someAPlusSomeB().asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSomeNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.addAll(data.someAPlusSomeB());
        assertNotSame(instance, instance2);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        assertEqualSet(expected, instance);
        expected.addAll(data.someAPlusSomeB().asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSameTypeAndSomeNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);

        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());

        ImmutableSet<Key> instance2 = newInstance();
        instance2 = instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2 = instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<Key> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        ImmutableSet<Key> instance3 = instance.addAll(instance2);
        assertNotSame(instance2, instance3);
        expected.addAll(expected2);
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSameTypeAndAllNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = newInstance(data.c);
        ImmutableSet<Key> instance3 = instance.addAll(instance2);
        assertNotSame(instance2, instance3);

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSameTypeToMutableAndAllNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = newInstance(data.c);
        ImmutableSet<Key> instance3 = instance.addAll(instance2.toMutable());
        assertNotSame(instance2, instance3);

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toMutableAddAllWithSameTypeAndAllNewKeysShouldReturnTrue(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = newInstance(data.c);
        Set<Key> mutableInstance = instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2.asSet()));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllElements(SetData data) {
        ImmutableSet<Key> instance = newInstance();
        assertEquals("[]", instance.toString());

        instance = instance.addAll(data.a.asSet());
        String str = instance.toString();
        assertEquals('[', str.charAt(0));
        assertEquals(']', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        SequencedSet<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithCloneShouldReturnThis(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> clone = toClonedInstance(instance);
        assertNotSame(instance, clone);
        ImmutableSet<Key> actual = instance.retainAll(clone);
        assertSame(instance, actual);
        assertEqualSet(data.a, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithContainedElementsShouldReturnThis(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> actual = instance.retainAll(data.a.asSet());
        assertSame(instance, actual);
        assertEqualSet(data.a, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSomeContainedElementsShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        ImmutableSet<Key> actual = instance.retainAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, actual);
        assertEqualSet(expected, instance);
        assertTrue(expected.retainAll(data.someAPlusSomeB.asSet()));
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithNewElementsShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.retainAll(data.c.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSameTypeAndAllNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = newInstance(data.c);
        ImmutableSet<Key> instance3 = instance.retainAll(instance2);
        assertNotSame(instance, instance3);
        assertEqualSet(data.a, instance);
        assertEqualSet(data.c, instance2);
        assertEqualSet(Collections.emptySet(), instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSameTypeAndSomeNewKeysShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);

        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());

        ImmutableSet<Key> instance2 = newInstance();
        instance2 = instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2 = instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<Key> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        ImmutableSet<Key> instance3 = instance.retainAll(instance2);
        assertNotSame(instance2, instance3);
        expected.retainAll(expected2);
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSelfShouldReturnThis(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        assertSame(instance, instance.retainAll(instance));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSomeNewKeysShouldReturnNewInstance(org.jhotdraw8.icollection.SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = instance.retainAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, instance2);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        assertEqualSet(expected, instance);
        expected.retainAll(data.someAPlusSomeB.asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllOfEmptySetShouldReturnThis(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance();
        assertSame(instance, instance.retainAll(data.c.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithEmptySetShouldReturnNewInstance(SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a.asSet());
        ImmutableSet<Key> instance2 = instance.retainAll(Collections.emptySet());
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @Test
    public void spliteratorShouldSupportNullKeys() throws Exception {
        ImmutableSet<Key> instance = newInstance();
        if (supportsNullKeys()) {
            assertEquals(instance.spliterator().characteristics() & Spliterator.NONNULL, 0, "spliterator should be non-null");
        } else {
            assertEquals(instance.spliterator().characteristics() & Spliterator.NONNULL, Spliterator.NONNULL, "spliterator should be nullable");
        }
    }

    @Test
    public void spliteratorShouldHaveImmutableSetCharacteristics() throws Exception {
        ImmutableSet<Key> instance = newInstance();

        assertEquals(Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.spliterator().characteristics());
    }

}
