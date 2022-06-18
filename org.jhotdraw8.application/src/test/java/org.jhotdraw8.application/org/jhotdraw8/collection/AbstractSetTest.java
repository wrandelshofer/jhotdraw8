package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSetTest {
    /**
     * The test data.
     */
    static final class Data {
        private final String name;
        private final ReadOnlySet<HashCollider> a;
        private final ReadOnlySet<HashCollider> c;

        /**
         * Creates a new instance with 3 maps of the same non-empty size.
         *
         * @param name the name of the data
         * @param a    a non-empty set
         * @param c    a map with different elements than a
         */
        Data(String name, ReadOnlySet<HashCollider> a,
             ReadOnlySet<HashCollider> c) {
            this.name = name;
            this.a = a;
            this.c = c;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public ReadOnlySet<HashCollider> a() {
            return a;
        }

        public ReadOnlySet<HashCollider> c() {
            return c;
        }
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

    protected abstract <E> @NonNull ImmutableSet<E> toImmutableInstance(@NonNull Set<E> m);

    protected abstract <E> @NonNull Set<E> toClonedInstance(@NonNull Set<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull Set<E> newInstance(@NonNull Iterable<E> m);

    public static @NonNull Stream<Data> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private final static Data NO_COLLISION_NICE_KEYS = newNiceData("no collisions nice keys", -1, 32, 100_000);
    private final static Data NO_COLLISION = newData("no collisions", -1, 32, 100_000);
    private final static Data ALL_COLLISION = newData("all collisions", 0, 32, 100_000);
    private final static Data SOME_COLLISION = newData("some collisions", 0x55555555, 32, 100_000);

    private static int createNewValue(@NonNull Random rng, @NonNull Set<Integer> usedValues, int bound) {
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

    private static @NonNull Data newData(@NonNull String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashSet<HashCollider> a = new LinkedHashSet<>(size * 2);
        LinkedHashSet<HashCollider> c = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyA = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            a.add(new HashCollider(keyA, hashBitMask));
            c.add(new HashCollider(keyC, hashBitMask));
        }
        return new Data(name,
                new WrappedReadOnlySet<>(a),
                new WrappedReadOnlySet<>(c));
    }

    private static @NonNull Data newNiceData(String name, int hashBitMask, int size, int bound) {
        int rng = 0;
        LinkedHashSet<HashCollider> a = new LinkedHashSet<>(size * 2);
        LinkedHashSet<HashCollider> c = new LinkedHashSet<>(size * 2);
        for (int i = 0; i < size; i++) {
            int keyA = rng++;
            a.add(new HashCollider(keyA, hashBitMask));
        }
        for (int i = 0; i < size; i++) {
            int keyC = rng++;
            c.add(new HashCollider(keyC, hashBitMask));
        }
        return new Data(name,
                new WrappedReadOnlySet<>(a),
                new WrappedReadOnlySet<>(c));
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

    @Test
    public void testNewInstanceNoArgsShouldBeEmpty() {
        Set<HashCollider> actual = newInstance();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @Test
    public void testNewInstanceCapacityArgsShouldBeEmpty() {
        Set<HashCollider> actual = newInstance(24, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(24, 0.75f);
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsShouldBeEqualToSet(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsOfSameTypeShouldBeEqualToSet(@NonNull Data data) {
        Set<HashCollider> actual1 = newInstance(data.a().asSet());
        Set<HashCollider> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsShouldBeEqualToSet(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsWithImmutableSetArgsOfSameTypeShouldBeEqualToSet(@NonNull Data data) {
        Set<HashCollider> actual1 = newInstance(data.a());
        Set<HashCollider> actual = newInstance(toImmutableInstance(actual1));
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldYieldEmptySet(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldBeIdempotent(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualSet(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(actual);
        assertEqualSet(data.a().asSet(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsShouldYieldExpectedValue(@NonNull Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        for (HashCollider k : data.a()) {
            assertTrue(actual.contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(actual.contains(k));
        }
        assertFalse(actual.contains(new Object()));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldRemoveElement(@NonNull Data data) {
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

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testStreamShouldYieldElements(@NonNull Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = instance.stream().collect(Collectors.toList());
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorShouldYieldElements(@NonNull Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = new ArrayList<>();
        instance.iterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSpliteratorShouldYieldElements(@NonNull Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> actualList = new ArrayList<>();
        instance.spliterator().forEachRemaining(actualList::add);
        assertEquals(expected.size(), actualList.size());
        assertEquals(expected, newInstance(actualList));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldThrowIllegalStateException(@NonNull Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.iterator();
        assertThrows(IllegalStateException.class, k::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameSet(@NonNull Data data) throws Exception {
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
    public void testEqualWithThisShouldYieldTrue(@NonNull Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneShouldYieldTrue(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone);
        assertEquals(instance, clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithNewElementShouldReturnTrue(@NonNull Data data) throws Exception {
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
    public void testAddWithContainedElementShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.a) {
            assertFalse(instance.add(e));
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithNewElementsShouldReturnTrue(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.addAll(data.c.asSet()));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithContainedElementsShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(data.a.asSet()));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSelfShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(instance));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithCloneShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = toClonedInstance(instance);
        assertFalse(instance.addAll(instance2));
        assertEquals(data.a.asSet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithNewElementShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            assertFalse(instance.remove(e));
            assertEqualSet(data.a, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithContainedKeyShouldReturnOldValue(@NonNull Data data) throws Exception {
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
    public void testRemoveAllWithNewKeyShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.removeAll(data.c.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithContainedKeyShouldReturnTrue(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllOfEmptySetShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance();
        assertFalse(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithEmptyThisShouldReturnFalse(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance();
        assertFalse(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithThisShouldReturnTrue(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(instance));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithSomeContainedKeyShouldReturnTrue(@NonNull Data data) throws Exception {
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
    public void testAddAllWithSomeNewKeysShouldReturnTrue(@NonNull Data data) throws Exception {
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
    public void testAddAllWithSameTypeAndSomeNewKeysShouldReturnTrue(@NonNull Data data) throws Exception {
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
    public void testAddAllWithSameTypeAndAllNewKeysShouldReturnTrue(@NonNull Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> instance2 = newInstance(data.c);
        assertTrue(instance.addAll(instance2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance);
    }
}
