package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlyList;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractReadOnlyListTest {
    protected <T> void assertEqualList(@NonNull List<T> expected, @NonNull ReadOnlyList<T> actual) {
        Object[] expectedValues = expected.toArray();
        Object[] actualValues = actual.toArray();
        assertArrayEquals(expectedValues, actualValues);

        for (var e : expected) {
            assertTrue(actual.contains(e), "must contain " + e);
        }

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asList());
        assertEquals(actual.asList(), expected);
    }
}
