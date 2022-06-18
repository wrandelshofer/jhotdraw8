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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractImmutableSetTest {
    /**
     * The test data.
     */
    static final class Data {
        private final String name;
        public final ReadOnlySet<HashCollider> a;
        public final ReadOnlySet<HashCollider> c;

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
    protected abstract <E> @NonNull ImmutableSet<E> newInstance();


    protected abstract <E> @NonNull Set<E> toMutableInstance(ImmutableSet<E> m);

    protected abstract <E> @NonNull ImmutableSet<E> toImmutableInstance(Set<E> m);

    protected abstract <E> @NonNull ImmutableSet<E> toClonedInstance(ImmutableSet<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m);

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

    private static @NonNull Data newData(String name, int hashBitMask, int size, int bound) {
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

    protected void assertEqualSet(@NonNull ReadOnlySet<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(@NonNull Set<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asSet());
        assertEquals(actual.asSet(), expected);

        ArrayList<HashCollider> expectedValues = new ArrayList<>(expected);
        ArrayList<HashCollider> actualValues = new ArrayList<>(actual.asSet());
        expectedValues.sort(Comparator.comparing(HashCollider::getValue));
        actualValues.sort(Comparator.comparing(HashCollider::getValue));
        assertEquals(expectedValues, actualValues);
    }

    protected void assertNotEqualSet(Set<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        assertNotEquals(expected, actual.asSet());
        assertNotEquals(actual.asSet(), expected);
    }

    @Test
    public void testNewInstanceNoArgsShouldBeEmpty() {
        ImmutableSet<HashCollider> actual = newInstance();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsShouldBeEqualToSet(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceSetArgsOfSameTypeShouldBeEqualToSet(@NonNull Data data) {
        ImmutableSet<HashCollider> actual1 = newInstance(data.a().asSet());
        ImmutableSet<HashCollider> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgShouldBeEqualToArg(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgWithThisShouldBeEqualToArg(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        ImmutableSet<HashCollider> actual2 = newInstance(actual);
        assertEqualSet(data.a().asSet(), actual2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlySetArgsWithMutableSetArgsOfSameTypeShouldBeEqualToSet(@NonNull Data data) {
        ImmutableSet<HashCollider> actual1 = newInstance(data.a());
        ImmutableSet<HashCollider> actual = newInstance(toMutableInstance(actual1));
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyClearShouldYieldEmptySet(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        ImmutableSet<HashCollider> actual2 = actual.copyClear();
        assertNotSame(actual, actual2);
        assertEqualSet(Collections.emptySet(), actual2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyClearShouldBeIdempotent(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        ImmutableSet<HashCollider> actual2 = actual.copyClear();
        assertEqualSet(Collections.emptySet(), actual2);
        assertNotSame(actual, actual2);

        ImmutableSet<HashCollider> actual3 = actual2.copyClear();
        assertSame(actual2, actual3);
        assertEqualSet(Collections.emptySet(), actual3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualSet(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        ImmutableSet<HashCollider> clone = toClonedInstance(actual);
        assertEqualSet(data.a().asSet(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsShouldYieldExpectedValue(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
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
    public void testIteratorRemoveShouldThrowUnsupportedOperationException(@NonNull Data data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> toRemove = new ArrayList<>(new HashSet<>(data.a().asSet()));
        outer:
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<HashCollider> i = actual.iterator(); i.hasNext(); ) {
                HashCollider k = i.next();
                if (k.equals(toRemove.get(0))) {
                    assertThrows(UnsupportedOperationException.class, i::remove);
                    toRemove.remove(0);
                    assertEqualSet(expected, actual);
                    continue outer;
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldThrowIllegalStateException(@NonNull Data data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.iterator();
        assertThrows(IllegalStateException.class, k::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameSet(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertEqualSet(data.a().asSet(), instance);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
            out.writeObject(instance);
        }
        ImmutableSet<HashCollider> deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
            deserialized = (ImmutableSet<HashCollider>) in.readObject();
        }
        assertEqualSet(data.a().asSet(), deserialized);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualWithThisShouldYieldTrue(@NonNull Data data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneShouldYieldTrue(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        ImmutableSet<HashCollider> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone.asSet());
        assertEquals(instance, clone);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddWithNewElementShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.c) {
            ImmutableSet<HashCollider> instance2 = instance.copyAdd(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            expected.add(e);
            assertEqualSet(expected, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddWithContainedElementShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.a) {
            ImmutableSet<HashCollider> instance2 = instance.copyAdd(e);
            assertSame(instance, instance2);
            assertEqualSet(expected, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithNewElementsShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyAddAll(data.c);
        assertNotSame(instance, instance2);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance2.asSet());
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithContainedElementsShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyAddAll(data.a.asSet());
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @SuppressWarnings({"unchecked", "CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithSelfShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyAddAll(instance);
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @SuppressWarnings({"unchecked", "CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithCloneShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = toClonedInstance(instance);
        ImmutableSet<HashCollider> instance3 = instance.copyAddAll(instance2);
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @SuppressWarnings({"unchecked", "CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithCloneToMutableShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = toClonedInstance(instance);
        ImmutableSet<HashCollider> instance3 = instance.copyAddAll(instance2.toMutable());
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveWithNewElementShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            ImmutableSet<HashCollider> instance2 = instance.copyRemove(e);
            assertSame(instance, instance2);
            assertEqualSet(data.a, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveWithContainedKeyShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        for (HashCollider e : data.a) {
            expected.remove(e);
            ImmutableSet<HashCollider> instance2 = instance.copyRemove(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            assertEqualSet(expected, instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithNewKeyShouldReturnSameInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyRemoveAll(data.c.asSet());
        assertSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithContainedKeyShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyRemoveAll(data.a.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithSomeContainedKeyShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.copyRemoveAll(list);
        assertNotSame(instance, instance2);
        assertEqualSet(new LinkedHashSet<>(listA.subList(listA.size() / 2, listA.size())), instance2);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithSomeNewKeysShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());
        ArrayList<HashCollider> list = new ArrayList<>(listA.subList(0, listA.size() / 2));
        list.addAll(listC.subList(0, listC.size() / 2));
        ImmutableSet<HashCollider> instance2 = instance.copyAddAll(list);
        assertNotSame(instance, instance2);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        expected.addAll(list);
        assertEqualSet(expected, instance2);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithSameTypeAndSomeNewKeysShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());

        ImmutableSet<HashCollider> instance2 = newInstance();
        instance2 = instance2.copyAddAll(listA.subList(0, listA.size() / 2));
        instance2 = instance2.copyAddAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<HashCollider> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        ImmutableSet<HashCollider> instance3 = instance.copyAddAll(instance2);
        assertNotSame(instance2, instance3);
        expected.addAll(expected2);
        assertEqualSet(expected, instance3);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithSameTypeAndAllNewKeysShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ImmutableSet<HashCollider> instance3 = instance.copyAddAll(instance2);
        assertNotSame(instance2, instance3);

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @SuppressWarnings({"unchecked", "SlowAbstractSetRemoveAll"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyAddAllWithSameTypeToMutableAndAllNewKeysShouldReturnNewInstance(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ImmutableSet<HashCollider> instance3 = instance.copyAddAll(instance2.toMutable());
        assertNotSame(instance2, instance3);

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToMutableAddAllWithSameTypeAndAllNewKeysShouldReturnTrue(@NonNull Data data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        Set<HashCollider> mutableInstance = instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2.asSet()));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

}
