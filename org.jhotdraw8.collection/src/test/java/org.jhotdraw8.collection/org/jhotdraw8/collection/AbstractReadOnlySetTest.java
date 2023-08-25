package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractReadOnlySetTest {
    protected <T> void assertEqualSet(@NonNull Set<T> expected, @NonNull ReadOnlySet<T> actual) {
        Set<T> expectedValues = new LinkedHashSet<>(expected);
        Set<T> actualValues = new LinkedHashSet<>(actual.asSet());
        assertEquals(expectedValues, actualValues);

        for (var e : expected) {
            assertTrue(actual.contains(e), "must contain " + e);
        }

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asSet());
        assertEquals(actual.asSet(), expected);
    }
}
