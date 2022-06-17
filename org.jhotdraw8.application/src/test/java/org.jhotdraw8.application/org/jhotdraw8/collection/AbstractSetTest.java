package org.jhotdraw8.collection;

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
    protected abstract Set<HashCollider> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract Set<HashCollider> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract Set<HashCollider> newInstance(Set<HashCollider> m);

    protected abstract Set<HashCollider> newInstance(ReadOnlySet<HashCollider> m);

    protected abstract ImmutableSet<HashCollider> toImmutableInstance(Set<HashCollider> m);

    protected abstract Set<HashCollider> toClonedInstance(Set<HashCollider> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract Set<HashCollider> newInstance(Iterable<HashCollider> m);

    public static Stream<Data> dataProvider() {
        return Stream.of(
                NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private final static Data NO_COLLISION = newData("no collisions", -1, 32, 100_000);
    private final static Data ALL_COLLISION = newData("all collisions", 0, 32, 100_000);
    private final static Data SOME_COLLISION = newData("some collisions", 0x55555555, 32, 100_000);

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

    private static Data newData(String name, int hashBitMask, int size, int bound) {
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

    protected void assertEqualSet(ReadOnlySet<HashCollider> expected, Set<HashCollider> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(Set<HashCollider> expected, Set<HashCollider> actual) {
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
    public void testNewInstanceSetArgsShouldBeEqualToSet(Data data) {
        Set<HashCollider> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsOfSameTypeShouldBeEqualToSet(Data data) {
        Set<HashCollider> actual1 = newInstance(data.a().asSet());
        Set<HashCollider> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsShouldBeEqualToSet(Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsWithImmutableSetArgsOfSameTypeShouldBeEqualToSet(Data data) {
        Set<HashCollider> actual1 = newInstance(data.a());
        Set<HashCollider> actual = newInstance(toImmutableInstance(actual1));
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldYieldEmptySet(Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldBeIdempotent(Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
        actual.clear();
        assertEqualSet(Collections.emptySet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualSet(Data data) {
        Set<HashCollider> actual = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(actual);
        assertEqualSet(data.a().asSet(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsShouldYieldExpectedValue(Data data) {
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
    public void testIteratorRemoveShouldRemoveElement(Data data) {
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
    public void testIteratorRemoveShouldThrowIllegalStateException(Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.iterator();
        assertThrows(IllegalStateException.class, k::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameSet(Data data) throws Exception {
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
    public void testEqualWithThisShouldYieldTrue(Data data) {
        Set<HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneShouldYieldTrue(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone);
        assertEquals(instance, clone);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithNewElementShouldReturnTrue(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            assertTrue(instance.add(e));
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithContainedElementShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.a) {
            assertFalse(instance.add(e));
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithNewElementsShouldReturnTrue(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.addAll(data.c.asSet()));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithContainedElementsShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(data.a.asSet()));
        assertEquals(data.a.asSet(), instance);
    }

    @SuppressWarnings({"unchecked", "CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSelfShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(instance));
        assertEquals(data.a.asSet(), instance);
    }

    @SuppressWarnings({"unchecked", "CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithCloneShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.addAll(toClonedInstance(instance)));
        assertEquals(data.a.asSet(), instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithNewElementShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            assertFalse(instance.remove(e));
            assertEqualSet(data.a, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithContainedKeyShouldReturnOldValue(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        for (HashCollider e : data.a) {
            expected.remove(e);
            assertTrue(instance.remove(e));
            assertEqualSet(expected, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithNewKeyShouldReturnFalse(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertFalse(instance.removeAll(data.c.asSet()));
        assertEqualSet(data.a, instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithContainedKeyShouldReturnTrue(Data data) throws Exception {
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(data.a.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveAllWithSomeContainedKeyShouldReturnTrue(Data data) throws Exception {
        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.removeAll(list));
        assertEqualSet(new LinkedHashSet<>(listA.subList(listA.size() / 2, listA.size())), instance);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddAllWithSomeNewKeyShouldReturnTrue(Data data) throws Exception {
        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        Set<HashCollider> instance = newInstance(data.a);
        assertTrue(instance.addAll(list));
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        expected.addAll(list);
        assertEqualSet(expected, instance);
    }
}
