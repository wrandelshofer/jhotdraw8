package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

abstract class AbstractPersistentSetTest {

    private void assertEquality(LinkedHashSet<HashCollider> expected, PersistentSet<HashCollider> actual) {
        assertEquals(expected.hashCode(), actual.hashCode(), "hashCode");
        assertEquals(actual, actual, "equal to itself");
        //noinspection ConstantConditions
        assertFalse(actual.equals(null), "equal to null");
        assertEquals(expected.size(), actual.size(), "equal size");
        assertEquals(expected, actual.asSet(), "expected.equals(actual.asSet)");
        assertEquals(expected.isEmpty(), actual.isEmpty(), "equal emptyness");
        assertEquals(actual, of(actual.toArray(new HashCollider[0])), "equal to reconstructed from array");
        assertFalse(actual.toString().isEmpty(), "always has a string");
        //noinspection EqualsBetweenInconvertibleTypes
        assertTrue(actual.equals(new ReadOnlySetWrapper<>(expected)), "actual to read-only wrapped expected");
    }

    protected abstract PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set);

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testPersistentSet(-1)),
                dynamicTest("3-bits hash", () -> testPersistentSet(7)),
                dynamicTest("0-bits hash", () -> testPersistentSet(0))
        );
    }

    protected abstract PersistentSet<HashCollider> of();

    protected abstract PersistentSet<HashCollider> of(@NonNull HashCollider... keys);

    private void testContains(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentSet<HashCollider> actual = of();
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();

        // GIVEN: a set with entries1
        actual = actual.copyAddAll(entries1);

        // WHEN: entry1 is in set, then contains must be true
        assertTrue(actual.contains(firstValue1));
        // WHEN: entry2 is not in set, then contains must be false
        assertFalse(actual.contains(firstValue2));

    }

    private void testCopyAdd(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();
        PersistentSet<HashCollider> actual = of();
        PersistentSet<HashCollider> newActual;
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();

        // GIVEN: a set with entries1
        for (HashCollider e : entries1) {
            actual = actual.copyAdd(e);
            expected.add(e);
            assertEquality(expected, actual);
        }

        // WHEN: entry1 is already in set, then withAdd must yield the same set
        newActual = actual.copyAdd(firstValue1);
        assertSame(newActual, actual);

        // WHEN: entry2 is not yet in set, then withAdd must yield a new set
        newActual = actual.copyAdd(firstValue2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        expected.add(firstValue2);
        assertEquality(expected, actual);
    }

    private void testCopyAddAll(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentSet<HashCollider> actual = of();
        PersistentSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.copyAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entries1 are already in set, then withAddAll must yield the same set
        newActual = actual.copyAddAll(entries1);
        assertSame(newActual, actual);

        // WHEN: entries2 are not yet in set, then withAddAll must yield a new set
        newActual = actual.copyAddAll(entries2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.addAll(entries1);
        expected.addAll(entries2);
        assertEquality(expected, actual);
    }

    private void testCopyOf(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentSet<HashCollider> actual;
        PersistentSet<HashCollider> newActual;

        // WHEN: a set is created with copyOf
        actual = copyOf(entries1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = copyOf(actual);
        assertSame(newActual, actual);
        actual = newActual;
        //
        expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);
    }

    private void testCopyRemove(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();
        PersistentSet<HashCollider> actual = of();
        PersistentSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.copyAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entry1 is in set, then withRemove must yield a new set
        newActual = actual.copyRemove(firstValue1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entry2 is not in set, then withRemove must yield the same set
        newActual = actual.copyRemove(firstValue2);
        assertSame(newActual, actual);

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(entries1);
        expected.remove(firstValue1);
        assertEquality(expected, actual);

        // Remove all one by one
        for (HashCollider e : entries1) {
            actual = actual.copyRemove(e);
            expected.remove(e);
        }
        assertEquality(expected, actual);
    }

    private void testCopyRemoveAll(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentSet<HashCollider> actual = of();
        PersistentSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.copyAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;
        assertEquality(values1, actual);

        // WHEN: entries2 are not in set, then withRemoveAll must yield the same set
        newActual = actual.copyRemoveAll(values2);
        assertSame(newActual, actual);

        // WHEN: entries1 are in set, then withRemoveAll must yield a new set
        newActual = actual.copyRemoveAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }

    private void testCopyRetainAll(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentSet<HashCollider> actual = of();
        PersistentSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.copyAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entries1 are in set, then withRetainAll must yield the same set
        newActual = actual.copyRetainAll(entries1);
        assertSame(newActual, actual);

        // WHEN: entries2 are not in set, then withRetainAll must yield a new set
        newActual = actual.copyRetainAll(entries2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }

    private void testEqualsHashCode(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        assertTrue(entries1.size() > 0);
        assertEquals(entries1.size(), entries2.size());
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();

        PersistentSet<HashCollider> actual1a = copyOf(entries1);
        assertEquals(actual1a, actual1a);//equals of itself

        PersistentSet<HashCollider> actual1b = copyOf(entries1);
        assertEquals(actual1a, actual1b);//equals of a new set that does not share trie nodes

        PersistentSet<HashCollider> actual1c = actual1a;
        actual1c = actual1c.copyRemove(firstValue1);
        actual1c = actual1c.copyAdd(firstValue1);
        assertEquals(actual1a, actual1c);// equals of a new set that shares many trie nodes


        PersistentSet<HashCollider> actual2a = copyOf(entries2);
        PersistentSet<HashCollider> actual2b = actual2a.copyRemove(firstValue2);
        HashCollider zero = new HashCollider(0, 0);
        LinkedHashSet<HashCollider> expected1 = new LinkedHashSet<>(entries1);
        LinkedHashSet<HashCollider> expected1plusZero = new LinkedHashSet<>(entries1);
        expected1plusZero.add(zero);

        // some assertions may not make sense, but they are needed for test coverage

        //noinspection AssertBetweenInconvertibleTypes
        assertEquals(expected1, actual1a.asSet());
        assertNotEquals(actual1a, actual2a);
        assertNotEquals(actual1a, actual2b);

        assertEquals(expected1.hashCode(), actual1a.hashCode());
        assertNotEquals(actual1a, expected1plusZero);
        assertNotEquals(actual1a, new Object());

    }

    @Test
    public void testNullValue() {
        PersistentSet<HashCollider> set = of(new HashCollider[]{null});
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(null);
        assertTrue(set.contains(null));
        assertEquals(expected.toString(), set.toString());
        assertEquals(expected, set.asSet());

        expected.remove(null);
        set = set.copyRemove(null);
        assertEquals(expected, set.asSet());
    }

    private void testOf(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        PersistentSet<HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = of(firstValue1, firstValue1, firstValue1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        assertEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = of(entries1.toArray(new HashCollider[0]));
        //
        expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);
    }

    void testPersistentSet(int hashBitMask) {
        // bulkSize must be at least 32 for good code coverage
        int bulkSize = 32;
        Random rng = new Random(0);
        for (int i = 0; i < 64; i++) {
            // entries1, entries2 are distinct sets of values
            LinkedHashSet<HashCollider> entries1 = new LinkedHashSet<>();
            LinkedHashSet<HashCollider> entries2 = new LinkedHashSet<>();
            while (entries1.size() < bulkSize) {
                entries1.add(new HashCollider(rng.nextInt(), hashBitMask));
            }
            while (entries2.size() < bulkSize) {
                HashCollider e = new HashCollider(rng.nextInt(), hashBitMask);
                if (!entries1.contains(e)) {
                    entries2.add(e);
                }
            }

            testCopyAdd(entries1, entries2);
            testCopyRemove(entries1, entries2);
            testCopyAddAll(entries1, entries2);
            testCopyRemoveAll(entries1, entries2);
            testCopyRetainAll(entries1, entries2);
            testContains(entries1, entries2);
            testOf(entries1, entries2);
            testCopyOf(entries1, entries2);
            testEqualsHashCode(entries1, entries2);
        }
    }

    private void testCopyAddAll(int[] array1, int[] array2, int hashMask) {
        Set<HashCollider> values1 = new LinkedHashSet<>();
        Set<HashCollider> values2 = new LinkedHashSet<>();
        for (int v1 : array1) {
            values1.add(new HashCollider(v1, hashMask));
        }
        for (int v2 : array2) {
            values2.add(new HashCollider(v2, hashMask));
        }
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(values1);
        expected.addAll(values2);

        PersistentSet<HashCollider> actualValues1 = copyOf(values1);
        PersistentSet<HashCollider> actualValues2 = copyOf(values2);

        PersistentSet<HashCollider> actual = actualValues1.copyAddAll(actualValues2);

        if (values1.equals(expected)) {
            assertSame(actualValues1, actual);
        } else {
            assertNotSame(actualValues1, actual);
        }

        assertEquality(expected, actual);
    }

    /**
     * Tests cases in method BitmapIndexedNode.addAll().
     * <pre>
     * Given the same bit-position in this and that:
     * case                   this.dataMap this.nodeMap that.dataMap  that.nodeMap
     * ---------------------------------------------------------------------------
     * 0    do nothing                -          -            -                -
     * 1    put "a" in dataMap        "a"        -            -                -
     * 2    put x in nodeMap          -          x            -                -
     * 3    illegal                   "a"        x            -                -
     * 4    put "b" in dataMap        -          -            "b"              -
     * 5.1  put "a" in dataMap        "a"        -            "a"              -   values are equal
     * 5.2  put {"a","b"} in nodeMap  "a"        -            "b"              -   values are not equal
     * 6    put x ∪ {"b"} in nodeMap  -          x            "b"              -
     * 7    illegal                   "a"        x            "b"              -
     * 8    put y in nodeMap          -          -            -                y
     * 9    put {"a"} ∪ y in nodeMap  "a"        -            -                y
     * 10.1 put x in nodeMap          -          x            -                x   nodes are equivalent
     * 10.2 put x ∪ y in nodeMap      -          x            -                y   nodes are not equivalent
     * 11   illegal                   "a"        x            -                y
     * 12   illegal                   -          -            "b"              y
     * 13   illegal                   "a"        -            "b"              y
     * 14   illegal                   -          x            "b"              y
     * 15   illegal                   "a"        x            "b"              y
     *  </pre>
     *
     * @return
     */
    @TestFactory
    public @NonNull List<DynamicTest> copyAddAllTests() {
        int a = 0b00_00100;
        int b = 0b01_00100;
        int c = 0b10_00100;
        int d = 0b11_00100;
        int e = 0b11_00010;
        int aa = 0b00_00110;
        int bb = 0b01_00110;
        int cc = 0b10_00110;
        int dd = 0b11_00110;

        return Arrays.asList(
                dynamicTest("case 0: ∅ u ∅, 32-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{}, -1)),
                dynamicTest("case 1: a u ∅, 32-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{}, -1)),
                dynamicTest("case 2: x={a,b} u e, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{e}, -1)),
                dynamicTest("case 4: ∅ u b, 32-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{b}, -1)),
                dynamicTest("case 5.1.!modified: a u a, 32-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a}, -1)),
                dynamicTest("case 5.1.modified: a u a,e, 32-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a, e}, -1)),
                dynamicTest("case 5.2: a u b, 32-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{b}, -1)),
                dynamicTest("case 6.!modified: x={a,b} u a, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a}, -1)),
                dynamicTest("case 6.modified: x={a,b} u c, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{c}, -1)),
                dynamicTest("case 8: e u y={a,b}, 32-bits hash", () -> testCopyAddAll(new int[]{e}, new int[]{a, b}, -1)),
                dynamicTest("case 9: a u y={a,b}, 32-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a, b}, -1)),
                dynamicTest("case 10.1.!modified: x={a,b} u x={a,b}, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b}, -1)),
                dynamicTest("case 10.1.modified: x={a,b} u x={a,b,c}, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b, c}, -1)),
                dynamicTest("case 10.2: x={a,b} u y={c,d}, 32-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{c, d}, -1)),
                //
                dynamicTest("case 0: ∅ u ∅, 0-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{}, 0)),
                dynamicTest("case 1: a u ∅, 0-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{}, 0)),
                dynamicTest("case 2: x={a,b} u ∅, 0-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{}, 0)),
                dynamicTest("case 4: ∅ u b, 0-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{b}, 0)),
                dynamicTest("case 5.1: a u a, 0-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a}, 0)),
                dynamicTest("case 5.2: a u b, 0-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{b}, 0)),
                dynamicTest("case 6: x={a,b} u a, 0-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a}, 0)),
                dynamicTest("case 8: ∅ u y={a,b}, 0-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{a, b}, 0)),
                dynamicTest("case 9: a u y={a,b}, 0-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a, b}, 0)),
                dynamicTest("case 10.1.!modified: x={a,b} u x={a,b}, 0-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b}, 0)),
                dynamicTest("case 10.1.modified: x={a,b} u x={a,b,c}, 0-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b, c}, 0)),
                dynamicTest("case 10.2: x={a,b} u y={c,d}, 0-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{c, d}, 0)),
                //
                dynamicTest("case 0: ∅ u ∅, 4-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{}, 15)),
                dynamicTest("case 1: a u ∅, 4-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{}, 15)),
                dynamicTest("case 2: x={a,b} u ∅, 4-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{}, 15)),
                dynamicTest("case 4: ∅ u b, 4-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{b}, 15)),
                dynamicTest("case 5.1: a u a, 4-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a}, 15)),
                dynamicTest("case 5.2: a u b, 4-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{b}, 15)),
                dynamicTest("case 6: x={a,b} u a, 4-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a}, 15)),
                dynamicTest("case 8: ∅ u y={a,b}, 4-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{a, b}, 15)),
                dynamicTest("case 9: a u y={a,b}, 4-bits hash", () -> testCopyAddAll(new int[]{a}, new int[]{a, b}, 15)),
                dynamicTest("case 10.1.!modified: x={a,b} u x={a,b}, 4-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b}, 15)),
                dynamicTest("case 10.1.modified: x={a,b} u x={a,b,c}, 4-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{a, b, c}, 15)),
                dynamicTest("case 10.2: x={a,b} u y={c,d}, 4-bits hash", () -> testCopyAddAll(new int[]{a, b}, new int[]{c, d}, 15)),
                //
                dynamicTest("case 1: a,aa u ∅, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa}, new int[]{}, -1)),
                dynamicTest("case 2: x={a,b},xx={aa,bb} u ∅, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa, b, bb}, new int[]{}, -1)),
                dynamicTest("case 4: ∅ u b,bb, 32-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{b, bb}, -1)),
                dynamicTest("case 5.1: a,aa u a,aa, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa}, new int[]{a, aa}, -1)),
                dynamicTest("case 5.2: a,aa u b,bb, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa}, new int[]{b, bb}, -1)),
                dynamicTest("case 6: x={a,b},xx={aa,bb} u a, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa, b, bb}, new int[]{a}, -1)),
                dynamicTest("case 8: ∅ u y={a,b},yy={aa,bb}, 32-bits hash", () -> testCopyAddAll(new int[]{}, new int[]{a, aa, b, bb}, -1)),
                dynamicTest("case 9: a,aa u y={a,b},yy={aa,bb}, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa}, new int[]{a, aa, b, bb}, -1)),
                dynamicTest("case 10.1: x={a,b},xx={aa,bb} u x={a,b},xx={aa,bb}, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa, b, bb}, new int[]{a, aa, b, bb}, -1)),
                dynamicTest("case 10.2: x={a,b},xx={aa,bb} u y={c,d},yy={cc,dd}, 32-bits hash", () -> testCopyAddAll(new int[]{a, aa, b, bb}, new int[]{c, cc, d, dd}, -1))

        );
    }

    private static HashCollider[] DATA_SET_ARRAY = new HashCollider[10_000];
    private static PersistentTrieSet<HashCollider> IDENTICAL_SET;

    static {
        Random rng = new Random(0);
        for (int i = 0; i < DATA_SET_ARRAY.length; i++) {
            DATA_SET_ARRAY[i] = new HashCollider(rng.nextInt(), -1);
        }
        IDENTICAL_SET = PersistentTrieSet.of(DATA_SET_ARRAY);
    }

    @Test
    public void measureCopyAddAllOfAlmostEmptySet() throws InterruptedException {
        PersistentTrieSet<HashCollider> set = PersistentTrieSet.<HashCollider>of(DATA_SET_ARRAY[0]);
        long start1 = System.nanoTime();
        PersistentTrieSet<HashCollider> set2 = set.copyAddAll(IDENTICAL_SET);
        long start2 = System.nanoTime();
        PersistentTrieSet<HashCollider> set3 = set.copyAddAll(IDENTICAL_SET);
        long start3 = System.nanoTime();
        System.out.println(set.size());
        System.out.println((start2 - start1) / 1_000 + "  " + (start3 - start2) / 1_000);
    }


}