/*
 * @(#)SequencedElement.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * A {@code SequencedElement} stores an element of a set and a sequence number.
 * <p>
 * {@code hashCode} and {@code equals} are based on the element - the sequence
 * number is not included.
 */
public class ChampSequencedElement<E> implements ChampSequencedData {

    private final @Nullable E element;
    private final int sequenceNumber;

    public ChampSequencedElement(@Nullable E element) {
        this.element = element;
        this.sequenceNumber = NO_SEQUENCE_NUMBER;
    }

    public ChampSequencedElement(@Nullable E element, int sequenceNumber) {
        this.element = element;
        this.sequenceNumber = sequenceNumber;
    }

    @NonNull
    public static <E> ChampSequencedElement<E> update(@NonNull ChampSequencedElement<E> oldK, @NonNull ChampSequencedElement<E> newK) {
        return oldK;
    }

    @NonNull
    public static <E> ChampSequencedElement<E> updateAndMoveToFirst(@NonNull ChampSequencedElement<E> oldK, @NonNull ChampSequencedElement<E> newK) {
        return oldK.getSequenceNumber() == newK.getSequenceNumber() + 1 ? oldK : newK;
    }

    @NonNull
    public static <E> ChampSequencedElement<E> updateAndMoveToLast(@NonNull ChampSequencedElement<E> oldK, @NonNull ChampSequencedElement<E> newK) {
        return oldK.getSequenceNumber() == newK.getSequenceNumber() - 1 ? oldK : newK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChampSequencedElement<?> that = (ChampSequencedElement<?>) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }

    public @Nullable E getElement() {
        return element;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public String toString() {
        return "{" +
                "" + element +
                ", seq=" + sequenceNumber +
                '}';
    }
}
