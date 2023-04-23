package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
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

    public static @NonNull Stream<SetData> dataProvider() {
        return Stream.of(
                NO_COLLISION_NICE_KEYS, NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private static final SetData NO_COLLISION_NICE_KEYS = SetData.newNiceData("no collisions nice keys", -1, 32, 100_000);
    private static final SetData NO_COLLISION = SetData.newData("no collisions", -1, 32, 100_000);
    private static final SetData ALL_COLLISION = SetData.newData("all collisions", 0, 32, 100_000);
    private static final SetData SOME_COLLISION = SetData.newData("some collisions", 0x55555555, 32, 100_000);

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


    protected void assertEqualSet(@NonNull ReadOnlySet<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        assertEqualSet(expected.asSet(), actual);
    }

    protected void assertEqualSet(@NonNull Set<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        ArrayList<HashCollider> expectedValues = new ArrayList<>(expected);
        ArrayList<HashCollider> actualValues = new ArrayList<>(actual.asSet());
        expectedValues.sort(Comparator.comparing(HashCollider::getValue));
        actualValues.sort(Comparator.comparing(HashCollider::getValue));
        assertEquals(expectedValues, actualValues);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asSet());
        assertEquals(actual.asSet(), expected);
    }

    protected void assertNotEqualSet(Set<HashCollider> expected, @NonNull ImmutableSet<HashCollider> actual) {
        assertNotEquals(expected, actual.asSet());
        assertNotEquals(actual.asSet(), expected);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        ImmutableSet<HashCollider> actual = newInstance();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEqualSet(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsShouldBeEqualToSet(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a().asSet());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceSetArgsOfSameTypeShouldBeEqualToSet(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual1 = newInstance(data.a().asSet());
        ImmutableSet<HashCollider> actual = newInstance(actual1);
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgShouldBeEqualToArg(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgWithThisShouldBeEqualToArg(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        ImmutableSet<HashCollider> actual2 = newInstance(actual);
        assertEqualSet(data.a().asSet(), actual2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlySetArgsWithMutableSetArgsOfSameTypeShouldBeEqualToSet(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual1 = newInstance(data.a());
        ImmutableSet<HashCollider> actual = newInstance(toMutableInstance(actual1));
        assertEqualSet(data.a().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldYieldEmptySet(@NonNull SetData data) {
        ImmutableSet<HashCollider> actual = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), actual);
        ImmutableSet<HashCollider> actual2 = actual.clear();
        assertNotSame(actual, actual2);
        assertEqualSet(Collections.emptySet(), actual2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldBeIdempotent(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertNotEqualSet(Collections.emptySet(), instance);
        ImmutableSet<HashCollider> instance2 = instance.clear();
        assertEqualSet(Collections.emptySet(), instance2);
        assertNotSame(instance, instance2);

        ImmutableSet<HashCollider> instance3 = instance2.clear();
        assertSame(instance2, instance3);
        assertEqualSet(Collections.emptySet(), instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualSet(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        ImmutableSet<HashCollider> clone = toClonedInstance(instance);
        assertEqualSet(data.a().asSet(), clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsShouldYieldExpectedValue(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        for (HashCollider k : data.a()) {
            assertTrue(instance.contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(instance.contains(k));
        }
        assertFalse(instance.contains(new Object()));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowUnsupportedOperationException(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        Set<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        List<HashCollider> toRemove = new ArrayList<>(new HashSet<>(data.a().asSet()));
        outer:
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<HashCollider> i = instance.iterator(); i.hasNext(); ) {
                HashCollider k = i.next();
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
    public void iteratorRemoveShouldThrowException(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.iterator();
        assertThrows(Exception.class, i::remove);
        Iterator<HashCollider> k = instance.iterator();
        assertThrows(Exception.class, k::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameSet(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        if (instance instanceof Serializable) {
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
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableAssertion"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneShouldYieldTrue(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        ImmutableSet<HashCollider> clone = toClonedInstance(instance);
        assertEquals(data.a().asSet(), clone.asSet());
        assertEquals(instance, clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddWithNewElementShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.c) {
            ImmutableSet<HashCollider> instance2 = instance.add(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            expected.add(e);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddWithContainedElementShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        for (HashCollider e : data.a) {
            ImmutableSet<HashCollider> instance2 = instance.add(e);
            assertSame(instance, instance2);
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithNewElementsShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.addAll(data.c);
        assertNotSame(instance, instance2);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEquals(expected, instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithContainedElementsShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.addAll(data.a.asSet());
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithSelfShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.addAll(instance);
        assertSame(instance, instance2);
        assertEquals(data.a.asSet(), instance2.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithCloneShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = toClonedInstance(instance);
        ImmutableSet<HashCollider> instance3 = instance.addAll(instance2);
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithCloneToMutableShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = toClonedInstance(instance);
        ImmutableSet<HashCollider> instance3 = instance.addAll(instance2.toMutable());
        assertSame(instance, instance3);
        assertEquals(data.a.asSet(), instance3.asSet());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveWithNewElementShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        for (HashCollider e : data.c) {
            ImmutableSet<HashCollider> instance2 = instance.remove(e);
            assertSame(instance, instance2);
            assertEqualSet(data.a, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveWithContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a().asSet());
        for (HashCollider e : data.a) {
            expected.remove(e);
            ImmutableSet<HashCollider> instance2 = instance.remove(e);
            assertNotSame(instance, instance2);
            instance = instance2;
            assertEqualSet(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithNewKeyShouldReturnSameInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.removeAll(data.c.asSet());
        assertSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.removeAll(data.a.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithReadOnlySetWithSomeContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.removeAll(data.someAPlusSomeB);
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithSetWithSomeContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.removeAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithSameTypeWithSomeContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.removeAll(newInstance(data.someAPlusSomeB));
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.removeAll(data.someAPlusSomeB().asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithSomeNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.addAll(data.someAPlusSomeB());
        assertNotSame(instance, instance2);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        assertEqualSet(expected, instance);
        expected.addAll(data.someAPlusSomeB().asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithSameTypeAndSomeNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());

        ImmutableSet<HashCollider> instance2 = newInstance();
        instance2 = instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2 = instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<HashCollider> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        ImmutableSet<HashCollider> instance3 = instance.addAll(instance2);
        assertNotSame(instance2, instance3);
        expected.addAll(expected2);
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithSameTypeAndAllNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ImmutableSet<HashCollider> instance3 = instance.addAll(instance2);
        assertNotSame(instance2, instance3);

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyAddAllWithSameTypeToMutableAndAllNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ImmutableSet<HashCollider> instance3 = instance.addAll(instance2.toMutable());
        assertNotSame(instance2, instance3);

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toMutableAddAllWithSameTypeAndAllNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        Set<HashCollider> mutableInstance = instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2.asSet()));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllElements(@NonNull SetData data) {
        ImmutableSet<HashCollider> instance = newInstance();
        assertEquals("[]", instance.toString());

        instance = instance.addAll(data.a.asSet());
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
    public void copyRetainAllWithCloneShouldReturnThis(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = toClonedInstance(instance);
        assertSame(instance, instance.retainAll(instance2));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithContainedElementsShouldReturnThis(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        assertSame(instance, instance.retainAll(data.a.asSet()));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSomeContainedElementsShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        Set<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        ImmutableSet<HashCollider> instance2 = instance.retainAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(expected, instance);
        assertTrue(expected.retainAll(data.someAPlusSomeB.asSet()));
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithNewElementsShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.retainAll(data.c.asSet());
        assertNotSame(instance, instance2);
        assertEqualSet(data.a, instance);
        assertEqualSet(Collections.emptySet(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSameTypeAndAllNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ImmutableSet<HashCollider> instance3 = instance.retainAll(instance2);
        assertNotSame(instance, instance3);
        assertEqualSet(data.a, instance);
        assertEqualSet(data.c, instance2);
        assertEqualSet(Collections.emptySet(), instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSameTypeAndSomeNewKeysShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);

        ArrayList<HashCollider> listA = new ArrayList<>(data.a.asSet());
        ArrayList<HashCollider> listC = new ArrayList<>(data.c.asSet());

        ImmutableSet<HashCollider> instance2 = newInstance();
        instance2 = instance2.addAll(listA.subList(0, listA.size() / 2));
        instance2 = instance2.addAll(listC.subList(0, listC.size() / 2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(listA);
        LinkedHashSet<HashCollider> expected2 = new LinkedHashSet<>();
        expected2.addAll(listA.subList(0, listA.size() / 2));
        expected2.addAll(listC.subList(0, listC.size() / 2));
        assertEqualSet(expected2, instance2);

        ImmutableSet<HashCollider> instance3 = instance.retainAll(instance2);
        assertNotSame(instance2, instance3);
        expected.retainAll(expected2);
        assertEqualSet(expected, instance3);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSelfShouldReturnThis(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        assertSame(instance, instance.retainAll(instance));
        assertEqualSet(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSomeNewKeysShouldReturnNewInstance(@NonNull org.jhotdraw8.collection.SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = instance.retainAll(data.someAPlusSomeB.asSet());
        assertNotSame(instance, instance2);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        assertEqualSet(expected, instance);
        expected.retainAll(data.someAPlusSomeB.asSet());
        assertEqualSet(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllOfEmptySetShouldReturnThis(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance();
        assertSame(instance, instance.retainAll(data.c.asSet()));
        assertEqualSet(Collections.emptySet(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithEmptySetShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a.asSet());
        ImmutableSet<HashCollider> instance2 = instance.retainAll(Collections.emptySet());
        assertNotSame(instance, instance2);
        assertEqualSet(Collections.emptySet(), instance2);
    }
}
