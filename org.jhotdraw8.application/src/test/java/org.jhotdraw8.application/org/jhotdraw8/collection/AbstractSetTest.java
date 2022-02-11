package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Abstract base class for testing classes that implement the {@link Set} interface.
 */
public abstract class AbstractSetTest {

    public static final HashCollider ZERO = new HashCollider(0, -1);
    public static final HashCollider THREE = new HashCollider(3, -1);
    public static final HashCollider FIVE = new HashCollider(5, -1);
    public static final HashCollider SIX = new HashCollider(6, -1);
    public static final HashCollider SEVEN = new HashCollider(7, -1);
    public static final HashCollider EIGHT = new HashCollider(8, -1);

    protected abstract <T> @NonNull Set<T> create(int expectedMaxSize, float maxLoadFactor);

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsAddAndRemove() {
        return Arrays.asList(
                dynamicTest("0", () -> doTestAddAndRemove(-1, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1)),
                dynamicTest("1", () -> doTestAddAndRemove(1, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1)),
                dynamicTest("2", () -> doTestAddAndRemove(0, 4, 34, 3, 2, 1, 0, 4, 34, 3, 2, 1))
        );
    }


    @SuppressWarnings("SlowAbstractSetRemoveAll")
    public void doTestAddAndRemove(int mask, int... elements) {
        List<HashCollider> list = new ArrayList<>(elements.length);
        for (int e : elements) {
            list.add(new HashCollider(e, mask));
        }
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        Set<HashCollider> instance = create(0, 0.75f);

        // WHEN: Element is added to instance
        // THEN: Instance must be equal to expected
        for (HashCollider e : list) {
            assertEquals(expected.contains(e), instance.contains(e));
            assertEquals(expected.add(e), instance.add(e));
            assertEquals(expected.contains(e), instance.contains(e));
        }

        // WHEN: Element is removed from instance
        // THEN Instance must be equal to expected
        for (HashCollider e : list) {
            assertEquals(expected.contains(e), instance.contains(e));
            assertEquals(expected.remove(e), instance.remove(e));
            assertEquals(expected.contains(e), instance.contains(e));
        }

        // WHEN: Element is bulk-added to instance
        // THEN Instance must be equal to expected
        assertEquals(expected.containsAll(list), instance.containsAll(list));
        assertEquals(expected.addAll(list), instance.addAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));

        // WHEN: Element is bulk-removed to instance
        // THEN Instance must be equal to expected
        assertEquals(expected.removeAll(list), instance.removeAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));

        // WHEN: Element is iterator-removed to instance
        // THEN Instance must be equal to expected
        expected.addAll(list);
        instance.addAll(list);
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
            assertEquals(expected, instance);
        }

        // WHEN: Element is bulk-retained to instance
        // THEN Instance must be equal to expected
        assertEquals(expected.addAll(list), instance.addAll(list));
        assertEquals(expected.retainAll(list), instance.retainAll(list));
        assertEquals(expected.containsAll(list), instance.containsAll(list));

        assertEquals(expected, instance);
    }


}