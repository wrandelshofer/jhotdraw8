package org.jhotdraw8.collection.champset;

import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * Stores a key and a sequence number.
 * <p>
 * {@code hashCode} and {@code equals} are based on the key only.
 */
public class SequencedKey<K> {
    /**
     * We use {@link Integer#MIN_VALUE} to detect overflows in the sequence number.
     * <p>
     * {@link Integer#MIN_VALUE} is the only integer number which can not
     * be negated.
     * <p>
     * We use negated numbers to iterate backwards through the sequence.
     */
    public static final int NO_SEQUENCE_NUMBER = Integer.MIN_VALUE;

    private final @Nullable K element;
    private final int sequenceNumber;

    public SequencedKey(@Nullable K element, int sequenceNumber) {
        this.element = element;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequencedKey<?> that = (SequencedKey<?>) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }

    public K getKey() {
        return element;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
