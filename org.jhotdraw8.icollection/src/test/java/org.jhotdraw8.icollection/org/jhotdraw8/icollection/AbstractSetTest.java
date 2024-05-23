package org.jhotdraw8.icollection;

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
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSetTest {
    private static final SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, 32, 100_000);
    private static final SetData NO_COLLISION = SetData.newData("no collisions", -1, 32, 100_000);
    private static final SetData ALL_COLLISION = SetData.newData("all collisions", 0, 32, 100_000);
    private static final SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, 32, 100_000);


    public static Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    protected abstract boolean supportsNullKeys();


    protected void assertEqualSet(ReadOnlySet<Key> expected, Set<Key> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(Set<Key> expected, Set<Key> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
        assertEquals(actual, expected);

        ArrayList<Key> expectedValues = new ArrayList<>(expected);
        ArrayList<Key> actualValues = new ArrayList<>(actual);
        expectedValues.sort(Comparator.comparing(Key::getValue));
        actualValues.sort(Comparator.comparing(Key::getValue));
        assertEquals(expectedValues, actualValues);
    }

    protected void assertNotEqualSet(Set<Key> expected, Set<Key> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
    }

    /**
     * Creates a new empty instance.
     */
    protected abstract <E> Set<E> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <E> Set<E> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> Set<E> newInstance(Set<E> m);

    protected abstract <E> Set<E> newInstance(ReadOnlySet<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> Set<E> newInstance(Iterable<E> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addNullContainsNullShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance();
        assertFalse(instance.contains(null));
        var expected = new LinkedHashSet<Key>();
        expected.add(null);
        instance.add(null);
        assertTrue(instance.contains(null));
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllNullContainsNullShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance();
        assertFalse(instance.contains(null));
        var expected = new LinkedHashSet<Key>();
        expected.addAll(Collections.singleton(null));
        instance.addAll(Collections.singleton(null));
        assertTrue(instance.contains(null));
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithCloneShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        if (!(instance instanceof Cloneable)) {
            return;
        }
        Set<Key> instance2 = toClonedInstance(instance);
        assertFalse(instance.addAll(instance2));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithContainedElementsShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertFalse(instance.addAll(data.a.asSet()));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithNewElementsShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.addAll(data.c.asSet()));
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSameTypeAndAllNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        Set<Key> instance2 = newInstance(data.c);
        assertTrue(instance.addAll(instance2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSameTypeAndSomeNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);

        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());

        Set<Key> instance2 = newInstance();
        instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<Key> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        assertTrue(instance.addAll(instance2));
        expected.addAll(expected2);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSelfShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertFalse(instance.addAll(instance));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addAllWithSomeNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);

        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());
        ArrayList<Key> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        assertTrue(instance.addAll(list));
        LinkedHashSet<Key> expected = new LinkedHashSet<>(listA);
        expected.addAll(list);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithContainedElementShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        for (Key e : data.a) {
            assertFalse(instance.add(e));
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithNewElementShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        for (Key e : data.c) {
            assertTrue(instance.add(e));
            expected.add(e);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldBeIdempotent(SetData data) {
        Set<Key> instance = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldYieldEmptySet(SetData data) {
        Set<Key> instance = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualSet(SetData data) {
        Set<Key> instance = newInstance(data.a());
        if (!(instance instanceof Cloneable)) {
            return;
        }
        Set<Key> clone = toClonedInstance(instance);
        assertEqualSet(data.a().asSet(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsShouldYieldExpectedValue(SetData data) {
        Set<Key> instance = newInstance(data.a());
        for (Key k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (Key k : data.c()) {
            assertFalse(instance.contains(k));
        }
        try {
            assertFalse(instance.contains(new Object()));
        } catch (ClassCastException e) {
            assertInstanceOf(SortedSet.class, instance, "only sorted sets may throw ClassCastException");
        }

        instance.addAll(data.someAPlusSomeB.asSet());
        for (Key k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (Key k : data.someAPlusSomeB) {
            assertTrue(instance.contains(k));
        }
        for (Key k : data.c()) {
            assertFalse(instance.contains(k));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsAllShouldYieldExpectedValue(SetData data) {
        Set<Key> instance = newInstance(data.a());
        assertTrue(instance.containsAll(data.a().asSet()));
        assertFalse(instance.containsAll(data.b().asSet()));
        assertFalse(instance.containsAll(data.someAPlusSomeB().asSet()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void hashCodeShouldYieldExpectedValue(SetData data) {
        Set<Key> instance = newInstance(data.a());
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        assertEquals(expected.hashCode(), instance.hashCode());

        instance.addAll(data.b().asSet());
        instance.retainAll(data.someAPlusSomeB().asSet());
        expected.addAll(data.b().asSet());
        expected.retainAll(data.someAPlusSomeB().asSet());
        assertEquals(expected.hashCode(), instance.hashCode());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(SetData data) {
        Set<Key> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneShouldYieldTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a());
        if (!(instance instanceof Cloneable)) {
            return;
        }
        Set<Key> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone);
        assertEquals(instance, clone);
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableAssertion"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldRemoveElement(SetData data) {
        Set<Key> actual = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> toRemove = new ArrayList<>(new HashSet<>(data.a().asSet()));
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<Key> i = actual.iterator(); i.hasNext(); ) {
                Key k = i.next();
                if (k.equals(toRemove.get(0))) {
                    i.remove();
                    toRemove.remove(0);
                    expected.remove(k);
                    assertEqualSet(expected, actual);
                }
            }
        }
        assertEqualSet(Collections.emptySet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowIllegalStateException(SetData data) {
        Set<Key> instance = newInstance(data.a());
        Iterator<Key> i = instance.iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<Key> k = instance.iterator();
        assertThrows(IllegalStateException.class, k::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldYieldElements(SetData data) {
        Set<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> actualList = new ArrayList<>();
        instance.iterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toArrayShouldYieldElements(SetData data) {
        Set<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> actualList = (List<Key>) (List<?>) Arrays.asList(instance.toArray());
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toArrayWithTemplateArgShouldYieldElements(SetData data) {
        Set<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> actualList = Arrays.asList(instance.toArray(new Key[0]));
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @Test
    public void newInstanceCapacityArgsShouldBeEmpty() {
        Set<Key> actual = newInstance(24, 0.75f);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(24, 0.75f);
        assertEqualSet(expected, actual);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        Set<Key> actual = newInstance();
        LinkedHashSet<Key> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgsShouldBeEqualToSet(SetData data) {
        Set<Key> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsOfSameTypeShouldBeEqualToSet(SetData data) {
        Set<Key> actual1 = newInstance(data.a().asSet());
        Set<Key> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsShouldBeEqualToSet(SetData data) {
        Set<Key> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllOfEmptySetShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance();
        assertFalse(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithContainedKeyShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithEmptyThisShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance();
        assertFalse(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithNewKeyShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertFalse(instance.removeAll(data.c.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithSomeContainedKeyShouldReturnTrue(SetData data) throws Exception {
        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());
        ArrayList<Key> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.removeAll(list));
        assertEqualSet(new LinkedHashSet<>(listA.subList(listA.size() / 2, listA.size())), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeAllWithThisShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithContainedKeyShouldReturnOldValue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        for (Key e : data.a) {
            expected.remove(e);
            assertTrue(instance.remove(e));
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithNewElementShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        for (Key e : data.c) {
            assertFalse(instance.remove(e));
            assertEqualSet(data.a, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameSet(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a());
        if (!(instance instanceof Serializable)) {
            return;
        }
        assertEqualSet(data.a().asSet(), instance);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
            out.writeObject(instance);
        }
        Set<Key> deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
            deserialized = (Set<Key>) in.readObject();
        }
        assertEqualSet(data.a().asSet(), deserialized);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void spliteratorShouldYieldElements(SetData data) {
        Set<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> actualList = new ArrayList<>();
        instance.spliterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @Test
    public void spliteratorShouldSupportNullKeys() throws Exception {
        Set<Key> instance = newInstance();
        if (supportsNullKeys()) {
            assertFalse(instance.spliterator().hasCharacteristics(Spliterator.NONNULL), "spliterator should be nullable");
        } else {
            assertTrue(instance.spliterator().hasCharacteristics(Spliterator.NONNULL), "spliterator should be non-null");
        }
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void streamShouldYieldElements(SetData data) {
        Set<Key> instance = newInstance(data.a());
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a().asSet());
        List<Key> actualList = instance.stream().collect(Collectors.toList());
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    protected abstract <E> Set<E> toClonedInstance(Set<E> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithCloneShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        if (!(instance instanceof Cloneable)) {
            return;
        }
        Set<Key> instance2 = toClonedInstance(instance);
        assertFalse(instance.retainAll(instance2));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithContainedElementsShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertFalse(instance.retainAll(data.a.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSomeContainedElementsShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        SequencedSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        assertTrue(instance.retainAll(data.someAPlusSomeB.asSet()));
        assertTrue(expected.retainAll(data.someAPlusSomeB.asSet()));
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithNewElementsShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.retainAll(data.c.asSet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllOfEmptySetShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance();
        assertFalse(instance.retainAll(data.c.asSet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithEmptySetShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a.asSet());
        assertTrue(instance.retainAll(Collections.emptySet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSameTypeAndAllNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        Set<Key> instance2 = newInstance(data.c);
        assertTrue(instance.retainAll(instance2));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSameTypeAndSomeNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);

        ArrayList<Key> listA = new ArrayList<>(data.a.asSet());
        ArrayList<Key> listC = new ArrayList<>(data.c.asSet());

        Set<Key> instance2 = newInstance();
        instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<Key> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        assertTrue(instance.retainAll(instance2));
        expected.retainAll(expected2);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSelfShouldReturnFalse(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertFalse(instance.retainAll(instance));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void retainAllWithSomeNewKeysShouldReturnTrue(SetData data) throws Exception {
        Set<Key> instance = newInstance(data.a);
        assertTrue(instance.retainAll(data.someAPlusSomeB.asSet()));
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.retainAll(data.someAPlusSomeB.asSet());
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllElements(SetData data) {
        Set<Key> instance = newInstance();
        assertEquals("[]", instance.toString());

        instance.addAll(data.a.asSet());
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
    public void sizeShouldReturnExpectedValue(SetData data) {
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        Set<Key> instance = newInstance();
        assertEquals(0, instance.size());

        instance = newInstance(data.a);
        assertEquals(data.a.size(), instance.size());

        instance.addAll(data.someAPlusSomeB.asSet());
        expected.addAll(data.someAPlusSomeB.asSet());
        assertEquals(expected.size(), instance.size());

        instance.addAll(data.c.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected.size(), instance.size());

        instance.removeAll(data.someAPlusSomeB.asSet());
        expected.removeAll(data.someAPlusSomeB.asSet());
        assertEquals(expected.size(), instance.size());

        instance.clear();
        assertEquals(0, instance.size());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void isEmptyShouldReturnExpectedValue(SetData data) {
        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        Set<Key> instance = newInstance();
        assertTrue(instance.isEmpty());

        instance = newInstance(data.a);
        assertEquals(data.a.isEmpty(), instance.isEmpty());

        instance.addAll(data.someAPlusSomeB.asSet());
        expected.addAll(data.someAPlusSomeB.asSet());
        assertEquals(expected.isEmpty(), instance.isEmpty());

        instance.addAll(data.c.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected.isEmpty(), instance.isEmpty());

        instance.removeAll(data.someAPlusSomeB.asSet());
        expected.removeAll(data.someAPlusSomeB.asSet());
        assertEquals(expected.isEmpty(), instance.isEmpty());

        instance.clear();
        assertTrue(instance.isEmpty());
    }
}
