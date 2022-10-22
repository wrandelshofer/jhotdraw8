package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSetTest {
    private final static SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, 32, 100_000);
    private final static SetData NO_COLLISION = SetData.newData("no collisions", -1, 32, 100_000);
    private final static SetData ALL_COLLISION = SetData.newData("all collisions", 0, 32, 100_000);
    private final static SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, 32, 100_000);


    public static @NonNull Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }


    protected void assertEqualSet(@NonNull ReadOnlySet<HashCollider> expected, @NonNull Set<HashCollider> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(@NonNull Set<HashCollider> expected, @NonNull Set<HashCollider> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
        assertEquals(actual, expected);

        ArrayList<HashCollider> expectedValues = new ArrayList<>(expected);
        ArrayList<HashCollider> actualValues = new ArrayList<>(actual);
        expectedValues.sort(Comparator.comparing(HashCollider::getValue));
        actualValues.sort(Comparator.comparing(HashCollider::getValue));
        assertEquals(expectedValues, actualValues);
    }

    protected void assertNotEqualSet(Set<HashCollider> expected, Set<HashCollider> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
    }

    /**
     * Creates a new empty instance.
     */
    protected abstract <E> @NonNull Set<E> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <E> @NonNull Set<E> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull Set<E> newInstance(@NonNull Set<E> m);

    protected abstract <E> @NonNull Set<E> newInstance(@NonNull ReadOnlySet<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull Set<E> newInstance(@NonNull Iterable<E> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithCloneShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = toClonedInstance(instance);
        assertFalse(instance.addAll(instance2));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithContainedElementsShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(data.a.asSet()));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithNewElementsShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.addAll(data.c.asSet()));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSameTypeAndAllNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = newInstance(data.c);
        assertTrue(instance.addAll(instance2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSameTypeAndSomeNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());

        Set<HashCollider> instance2 = newInstance();
        instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<HashCollider> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        assertTrue(instance.addAll(instance2));
        expected.addAll(expected2);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSelfShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(instance));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSomeNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        assertTrue(instance.addAll(list));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        expected.addAll(list);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithContainedElementShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.a) {
            assertFalse(instance.add(e));
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithNewElementShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.c) {
            assertTrue(instance.add(e));
            expected.add(e);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldBeIdempotent(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldYieldEmptySet(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), instance);
        instance.clear();
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualSet(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(instance);
        assertEqualSet(data.a().asSet(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsShouldYieldExpectedValue(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        for (HashCollider k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(instance.contains(k));
        }
        assertFalse(instance.contains(new Object()));

        instance.addAll(data.someAPlusSomeB.asSet());
        for (HashCollider k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (HashCollider k : data.someAPlusSomeB) {
            assertTrue(instance.contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(instance.contains(k));
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsAllShouldYieldExpectedValue(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertTrue(instance.containsAll(data.a().asSet()));
        assertFalse(instance.containsAll(data.b().asSet()));
        assertFalse(instance.containsAll(data.someAPlusSomeB().asSet()));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testHashCodeShouldYieldExpectedValue(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        assertEquals(expected.hashCode(), instance.hashCode());

        instance.addAll(data.b().asSet());
        instance.retainAll(data.someAPlusSomeB().asSet());
        expected.addAll(data.b().asSet());
        expected.retainAll(data.someAPlusSomeB().asSet());
        assertEquals(expected.hashCode(), instance.hashCode());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualWithThisShouldYieldTrue(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneShouldYieldTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone);
        assertEquals(instance, clone);
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableAssertion"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithNullShouldYieldFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldRemoveElement(@NonNull SetData data) {
        Set<HashCollider> actual = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> toRemove = new ArrayList<>(new HashSet<>(data.a().asSet()));
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<HashCollider> i = actual.iterator(); i.hasNext(); ) {
                HashCollider k = i.next();
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
    public void testIteratorRemoveShouldThrowIllegalStateException(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.iterator();
        assertThrows(IllegalStateException.class, k::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorShouldYieldElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = new ArrayList<>();
        instance.iterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toArrayShouldYieldElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = (List<HashCollider>) (List<?>) Arrays.asList(instance.toArray());
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }


    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toArrayWithTemplateArgShouldYieldElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = Arrays.asList(instance.toArray(new HashCollider[0]));
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @Test
    public void testNewInstanceCapacityArgsShouldBeEmpty() {
        Set<HashCollider> actual = newInstance(24, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(24, 0.75f);
        assertEqualSet(expected, actual);
    }

    @Test
    public void testNewInstanceNoArgsShouldBeEmpty() {
        Set<HashCollider> actual = newInstance();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsShouldBeEqualToSet(@NonNull SetData data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsOfSameTypeShouldBeEqualToSet(@NonNull SetData data) {
        Set<HashCollider> actual1 = newInstance(data.a().asSet());
        Set<HashCollider> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsShouldBeEqualToSet(@NonNull SetData data) {
        Set<HashCollider> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllOfEmptySetShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance();
        assertFalse(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithContainedKeyShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithEmptyThisShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance();
        assertFalse(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithNewKeyShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.removeAll(data.c.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithSomeContainedKeyShouldReturnTrue(@NonNull SetData data) throws Exception {
        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(list));
        assertEqualSet(new LinkedHashSet<>(listA.subList(listA.size() / 2, listA.size())), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithThisShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithContainedKeyShouldReturnOldValue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        for (HashCollider e : data.a) {
            expected.remove(e);
            assertTrue(instance.remove(e));
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithNewElementShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            assertFalse(instance.remove(e));
            assertEqualSet(data.a, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameSet(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        assertEqualSet(data.a().asSet(), instance);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
            out.writeObject(instance);
        }
        Set<HashCollider> deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
            deserialized = (Set<HashCollider>) in.readObject();
        }
        assertEqualSet(data.a().asSet(), deserialized);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSpliteratorShouldYieldElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = new ArrayList<>();
        instance.spliterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testStreamShouldYieldElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = instance.stream().collect(Collectors.toList());
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    protected abstract <E> @NonNull Set<E> toClonedInstance(@NonNull Set<E> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithCloneShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = toClonedInstance(instance);
        assertFalse(instance.retainAll(instance2));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithContainedElementsShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.retainAll(data.a.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithSomeContainedElementsShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        assertTrue(instance.retainAll(data.someAPlusSomeB.asSet()));
        assertTrue(expected.retainAll(data.someAPlusSomeB.asSet()));
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithNewElementsShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.retainAll(data.c.asSet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllOfEmptySetShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance();
        assertFalse(instance.retainAll(data.c.asSet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithEmptySetShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a.asSet());
        assertTrue(instance.retainAll(Collections.emptySet()));
        assertEquals(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithSameTypeAndAllNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = newInstance(data.c);
        assertTrue(instance.retainAll(instance2));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithSameTypeAndSomeNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());

        Set<HashCollider> instance2 = newInstance();
        instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<HashCollider> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        assertTrue(instance.retainAll(instance2));
        expected.retainAll(expected2);
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithSelfShouldReturnFalse(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.retainAll(instance));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRetainAllWithSomeNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.retainAll(data.someAPlusSomeB.asSet()));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.retainAll(data.someAPlusSomeB.asSet());
        assertEqualSet(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToStringShouldContainAllElements(@NonNull SetData data) {
        Set<HashCollider> instance = newInstance();
        assertEquals("[]", instance.toString());

        instance.addAll(data.a.asSet());
        String str = instance.toString();
        assertEquals('[', str.charAt(0));
        assertEquals(']', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        Set<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSizeShouldReturnExpectedValue(@NonNull SetData data) {
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        Set<HashCollider> instance = newInstance();
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
    public void testIsEmptyShouldReturnExpectedValue(@NonNull SetData data) {
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        Set<HashCollider> instance = newInstance();
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
