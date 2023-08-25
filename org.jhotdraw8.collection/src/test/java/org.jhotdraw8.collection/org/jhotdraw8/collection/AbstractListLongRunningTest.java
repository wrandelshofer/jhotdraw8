package org.jhotdraw8.collection;

import org.jhotdraw8.collection.computed.ComputedList;
import org.jhotdraw8.collection.readonly.SizeLimitExceededException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractListLongRunningTest {
    abstract List<Long> createInstance();

    abstract long maxSize();

    /**
     * <ol>
     *     <li>GIVEN a new list instance</li>
     *     <li>WHEN we add {@link #maxSize()} elements</li>
     *     <li>THEN the list <b>must not</b> throw a {@link SizeLimitExceededException}</li>
     *     <li>WHEN we add one element more
     *     <li>THEN the list <b>must</b> throw a {@link SizeLimitExceededException}</li>
     * </ol>
     * Should not throw a size limit exception, when we add {@link #maxSize()} elements.
     */
    @Test
    public void shouldThrowSizeLimitExceededException() {
        List<Long> instance = createInstance();
        ComputedList<Long> toBeAdded = new ComputedList<>(maxSize(), i -> 42L);
        instance.addAll(toBeAdded.asList());
        assertEquals(maxSize(), instance.size(), "size");
        assertThrows(SizeLimitExceededException.class, () -> instance.add(maxSize() + 1));
    }
}
