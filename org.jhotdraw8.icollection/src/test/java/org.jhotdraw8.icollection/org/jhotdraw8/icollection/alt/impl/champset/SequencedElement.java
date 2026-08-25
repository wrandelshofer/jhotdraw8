/*
 * @(#)SequencedElement.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champset;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// A `SequencedElement` stores an element of a set and a sequence number.
///
/// `hashCode` and `equals` are based on the element - the sequence
/// number is not included.
public class SequencedElement<E> implements SequencedData {

    private final E element;
    private final int sequenceNumber;

    public SequencedElement(E element) {
        this.element = element;
        this.sequenceNumber = NO_SEQUENCE_NUMBER;
    }

    public SequencedElement(E element, int sequenceNumber) {
        this.element = element;
        this.sequenceNumber = sequenceNumber;
    }

    public static <E> SequencedElement<E> keepOldValue(SequencedElement<E> oldK, SequencedElement<E> newK) {
        return oldK;
    }

    public static <E> SequencedElement<E> insertOrFail(SequencedElement<E> oldK, SequencedElement<E> newK) {
        throw new IllegalArgumentException("Element is already in set. elem=" + oldK);
    }

    public static int keyHash(@Nullable Object a) {
        return Objects.hashCode(a);
    }

    public static <K> int elementKeyHash(SequencedElement<K> a) {
        return Objects.hashCode(a.getElement());
    }


    public static <E> SequencedElement<E> putAndMoveToFirst(SequencedElement<E> oldK, SequencedElement<E> newK) {
        return oldK.sequenceNumber() == newK.sequenceNumber() + 1 ? oldK : newK;
    }

    public static <E> SequencedElement<E> putAndMoveToLast(SequencedElement<E> oldK, SequencedElement<E> newK) {
        return oldK.sequenceNumber() == newK.sequenceNumber() - 1 ? oldK : newK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequencedElement<?> that = (SequencedElement<?>) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }

    public E getElement() {
        return element;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public String toString() {
        return "{" + element +
                ", seq=" + sequenceNumber +
                '}';
    }
}
