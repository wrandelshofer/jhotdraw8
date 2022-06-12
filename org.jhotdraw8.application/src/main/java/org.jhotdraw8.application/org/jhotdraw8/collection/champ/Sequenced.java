package org.jhotdraw8.collection.champ;

public interface Sequenced {
    /**
     * We use {@link Integer#MIN_VALUE} to detect overflows in the sequence number.
     * <p>
     * {@link Integer#MIN_VALUE} is the only integer number which can not
     * be negated.
     * <p>
     * We use negated numbers to iterate backwards through the sequence.
     */
    int NO_SEQUENCE_NUMBER = Integer.MIN_VALUE;

    int getSequenceNumber();
}
