/*
 * @(#)AbstractSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcollection.indexedset;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Abstract base class for testing classes that implement the {@link Set} interface.
 */
@SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked", "EqualsWithItself", "SimplifiableAssertion"})
public abstract class AbstractSetTestOld {

    public static final HashCollider ZERO = new HashCollider(0, -1);
    public static final HashCollider THREE = new HashCollider(3, -1);
    public static final HashCollider FIVE = new HashCollider(5, -1);
    public static final HashCollider SIX = new HashCollider(6, -1);
    public static final HashCollider SEVEN = new HashCollider(7, -1);
    public static final HashCollider EIGHT = new HashCollider(8, -1);

    protected void assertEqualSets(Set<HashCollider> expected, Set<HashCollider> instance) {
        assertEquals(expected.size(), instance.size());
        assertEquals(expected.hashCode(), instance.hashCode());
        assertEquals(expected, instance);
        assertEquals(expected, new LinkedHashSet<>(instance));
        assertEquals(instance, expected);
        assertEquals(expected.isEmpty(), instance.isEmpty());

        Set<Object> copy = create(instance.size(), 1.0f);
        copy.addAll(instance);
        assertEquals(instance, copy);
    }

    protected abstract <T> @NonNull Set<T> create(int expectedMaxSize, float maxLoadFactor);

    public void doTest(int mask, int... elements) throws Exception {
        List<HashCollider> list = new ArrayList<>(elements.length);
        for (int e : elements) {
            list.add(new HashCollider(e, mask));
        }
        doTest(list);
    }

    public void doTest(List<HashCollider> list) throws Exception {
        doTestAddOneByOne(list);
        doTestBulkAdd(list);
        doTestClone(list);
        doTestRemoveOneByOne(list);
        doTestIteratorRemove(list);
        doTestBulkRemove(list);
        doTestBulkRetain(list);
        doTestClear(list);
        doTestEquals(list);
    }

    public void doTestAddOneByOne(List<HashCollider> list) {

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        Set<HashCollider> instance = create(0, 0.75f);

        // WHEN: Element is added to instance
        // THEN: Instance must be equal to expected
        for (HashCollider e : list) {
            assertEquals(expected.contains(e), instance.contains(e));
            assertEquals(expected.add(e), instance.add(e));
            assertTrue(instance.contains(e));
            assertEqualSets(expected, instance);
        }
    }

    public void doTestEquals(List<HashCollider> list) {
        Set<HashCollider> instance = create(0, 0.75f);
        Set<HashCollider> instance2 = create(0, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();

        // WHEN: Element is added to instance
        // THEN: Instance must be equal to expected
        for (HashCollider e : list) {
            if (instance.add(e)) {
                assertNotEquals(instance, instance2);
            }
            instance2.add(e);
            expected.add(e);
            assertEquals(instance, instance2);
            assertEquals(instance, expected);
            assertEquals(expected, instance);
        }
    }

    public void doTestBulkAdd(List<HashCollider> list) {
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        Set<HashCollider> instance = create(0, 0.75f);
        // WHEN: Elements are bulk-added to instance
        // THEN Instance must be equal to expected
        assertEquals(expected.containsAll(list), instance.containsAll(list));
        assertEquals(expected.addAll(list), instance.addAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));

        // WHEN: Elements are bulk-added to instance again
        // THEN Instance must be equal to expected
        assertEquals(expected.addAll(list), instance.addAll(list));

        // WHEN: self is bulk-added to instance
        // THEN Instance must be equal to expected
        //noinspection CollectionAddedToSelf
        assertEquals(expected.addAll(expected), instance.addAll(instance));

        // WHEN: expected is bulk-added to instance
        // THEN Instance must be equal to expected
        assertEquals(expected.addAll(instance), instance.addAll(expected));
    }

    public void doTestBulkRemove(List<HashCollider> list) {
        Set<HashCollider> instance = create(0, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        instance.addAll(list);

        // WHEN: Elements are bulk-removed from instance
        // THEN Instance must be equal to expected
        //noinspection SuspiciousMethodCalls
        assertEquals(expected.removeAll(Set.of()), instance.removeAll(Set.of()));
        assertEquals(expected.removeAll(list), instance.removeAll(list));
        //noinspection SuspiciousMethodCalls
        assertEquals(expected.containsAll(Set.of()), instance.containsAll(Set.of()));
        assertEquals(expected.containsAll(list), instance.containsAll(list));
        assertEquals(expected.addAll(Set.of()), instance.addAll(Set.of()));

        expected.addAll(list);
        instance.addAll(list);
        Collections.reverse(list);

        // WHEN: Elements are bulk-removed from instance
        // THEN Instance must be equal to expected
        assertEquals(expected.removeAll(list), instance.removeAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));

        // WHEN: Elements are bulk-removed from self
        // THEN Instance must be equal to expected
        expected.addAll(list);
        instance.addAll(list);
        //noinspection CollectionAddedToSelf
        assertEquals(expected.removeAll(expected), instance.removeAll(instance));

        // WHEN: Elements are bulk-removed from expected
        // THEN Instance must be equal to expected
        expected.addAll(list);
        instance.addAll(list);
        assertEquals(!list.isEmpty(), instance.removeAll(expected));
        assertEquals(Set.of(), instance);
        instance.addAll(list);
        //noinspection CollectionAddedToSelf
        assertEquals(!list.isEmpty(), instance.removeAll(instance));
        assertEquals(Set.of(), instance);
    }

    public void doTestBulkRetain(List<HashCollider> list) {
        Set<HashCollider> instance = create(0, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();

        // WHEN: Elements are bulk-retained to instance
        // THEN Instance must be equal to expected
        //noinspection RedundantOperationOnEmptyContainer
        expected.clear();
        instance.clear();
        assertEquals(expected.addAll(list), instance.addAll(list));
        assertEquals(expected.retainAll(list), instance.retainAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));
        assertEqualSets(expected, instance);

        // WHEN: Sub-List is bulk-retained to instance
        // THEN Instance must be equal to expected
        expected.clear();
        instance.clear();
        assertEquals(expected.addAll(list), instance.addAll(list));
        List<HashCollider> subListLow = list.subList(0, list.size() / 2);
        List<HashCollider> subListHigh = list.subList(list.size() / 2 + 1, list.size());
        assertEquals(expected.retainAll(subListLow), instance.retainAll(subListLow));
        assertEqualSets(expected, instance);
        assertEquals(expected.retainAll(subListHigh), instance.retainAll(subListHigh));
        assertEqualSets(expected, instance);

        // WHEN: List with more elements is bulk-retained to instance
        // THEN Instance must be equal to expected
        expected.clear();
        instance.clear();
        assertEquals(expected.addAll(subListLow), instance.addAll(subListLow));
        assertEquals(expected.retainAll(list), instance.retainAll(list));
        assertEqualSets(expected, instance);

        // WHEN: self is bulk-retained to instance
        // THEN Instance must be equal to expected
        expected.clear();
        instance.clear();
        assertEquals(expected.addAll(list), instance.addAll(list));
        //noinspection CollectionAddedToSelf
        assertEquals(expected.retainAll(expected), instance.retainAll(instance));
        assertEquals(expected.retainAll(instance), instance.retainAll(expected));

        // WHEN: self is bulk-retained to empty
        // THEN Instance must be equal to expected
        expected.clear();
        instance.clear();
        assertEquals(expected.addAll(list), instance.addAll(list));
        //noinspection SuspiciousMethodCalls
        assertEquals(expected.retainAll(Set.of()), instance.retainAll(Set.of()));
        assertEquals(Set.of(), instance);

        // WHEN: empty self is bulk-retained to non-empty
        // THEN Instance must be equal to expected
        expected.clear();
        instance.clear();
        assertEquals(expected.retainAll(list), instance.retainAll(list));
        assertEquals(Set.of(), instance);
    }

    public void doTestClear(List<HashCollider> list) {
        Set<HashCollider> instance = create(0, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        instance.addAll(list);

        // WHEN: set is cleared
        // THEN: set must be equal to expected
        expected.clear();
        instance.clear();
        assertEqualSets(expected, instance);
    }

    public void doTestClone(List<HashCollider> list) throws
            InvocationTargetException, IllegalAccessException {
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        Set<HashCollider> instance = create(0, 0.75f);
        instance.addAll(list);


        // WHEN is cloned
        // THEN Instance must be equal to expected
        try {
            Set<HashCollider> actualClone = (Set<HashCollider>) instance.getClass().getMethod("clone").invoke(instance);
            assertEqualSets(expected, instance);

            // WHEN: Elements are bulk-removed from instance
            // THEN Instance must be equal to expected
            assertEquals(expected.removeAll(list), instance.removeAll(list));
            assertEquals(expected.containsAll(list), instance.containsAll(list));
            assertEqualSets(expected, instance);

            // WHEN: Elements are bulk-added to cloned instance
            // THEN Instance must be equal to expected
            expected.addAll(list);
            assertTrue(instance.addAll(actualClone));
            assertEqualSets(expected, instance);

            // WHEN: Elements are bulk-added again to cloned instance
            // THEN Instance must be equal to expected
            assertFalse(instance.addAll(actualClone));
            assertEqualSets(expected, instance);

            // WHEN: Elements are bulk-removed from cloned instance
            // THEN Instance must be equal to expected
            expected.removeAll(list);
            instance.removeAll(actualClone);
            assertEqualSets(expected, instance);

            // WHEN: Elements are bulk-removed again from cloned instance
            // THEN Instance must be equal to expected
            assertFalse(instance.removeAll(actualClone));
            assertEquals(expected, instance);
        } catch (NoSuchMethodException e) {
            // its okay, we can not clone this set
        }
    }

    public void doTestIteratorRemove(List<HashCollider> list) {
        Set<HashCollider> instance = create(0, 0.75f);
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        instance.addAll(list);

        // WHEN: Element is iterator-removed to instance
        // THEN Instance must be equal to expected
        for (HashCollider e : list) {
            for (Iterator<HashCollider> it = expected.iterator(); it.hasNext(); ) {
                if (Objects.equals(e, it.next())) {
                    it.remove();
                    break;
                }
            }
            for (Iterator<HashCollider> it = instance.iterator(); it.hasNext(); ) {
                if (Objects.equals(e, it.next())) {
                    it.remove();
                    break;
                }
            }
            assertEquals(expected, instance, "element " + e + " was removed from the iterator");
        }
    }

    public void doTestRemoveOneByOne(List<HashCollider> list) {
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        Set<HashCollider> instance = create(0, 0.75f);
        instance.addAll(list);

        // WHEN: Element is removed from instance
        // THEN Instance must be equal to expected
        for (HashCollider e : list) {
            assertEquals(expected.contains(e), instance.contains(e));
            if (expected.contains(e)) {
                assertTrue(instance.remove(e));
            } else {
                assertFalse(instance.remove(e));
            }
            assertFalse(instance.contains(e));
            expected.remove(e);
            assertEqualSets(expected, instance);
        }

        expected.addAll(list.subList(0, list.size() / 2));
        instance.addAll(list.subList(0, list.size() / 2));
        // WHEN: Element is removed from instance
        // THEN Instance must be equal to expected
        for (HashCollider e : list) {
            assertEquals(expected.contains(e), instance.contains(e));
            if (expected.contains(e)) {
                assertTrue(instance.remove(e));
            } else {
                assertFalse(instance.remove(e));
            }
            assertFalse(instance.contains(e));
            expected.remove(e);
            assertEqualSets(expected, instance);
        }
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("full mask", () -> doTest(-1, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1)),
                dynamicTest("some collisions", () -> doTest(1, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1)),
                dynamicTest("all collisions", () -> doTest(0, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1))
        );
    }


}