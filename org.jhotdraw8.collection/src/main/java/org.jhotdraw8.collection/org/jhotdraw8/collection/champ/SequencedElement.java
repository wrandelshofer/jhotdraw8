/*
 * @(#)SequencedElement.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * A {@code SequencedElement} stores an element of a set and a sequence number.
 * <p>
 * {@code hashCode} and {@code equals} are based on the element - the sequence
 * number is not included.
 */
class SequencedElement<E> implements SequencedData {

    private final @Nullable E element;
    private final int sequenceNumber;

    public SequencedElement(@Nullable E element) {
        this.element = element;
        this.sequenceNumber = NO_SEQUENCE_NUMBER;
    }

    public SequencedElement(@Nullable E element, int sequenceNumber) {
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Renumbers the sequence numbers in all nodes from {@code 0} to {@code size}.
     * <p>
     * Afterwards the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param <K>     the key type
     * @param root    the root of the trie
     * @param mutator the mutator which will own all nodes of the trie
     * @return the new root
     */
    public static <K> BitmapIndexedNode<SequencedElement<K>> renumber(int size,
                                                                      @NonNull BitmapIndexedNode<SequencedElement<K>> root,
                                                                      @NonNull BitmapIndexedNode<SequencedElement<K>> sequenceRoot,
                                                                      @NonNull IdentityObject mutator,
                                                                      @NonNull ToIntFunction<SequencedElement<K>> hashFunction,
                                                                      @NonNull BiPredicate<SequencedElement<K>, SequencedElement<K>> equalsFunction) {
        if (size == 0) {
            return root;
        }
        BitmapIndexedNode<SequencedElement<K>> newRoot = root;
        ChangeEvent<SequencedElement<K>> details = new ChangeEvent<>();
        int seq = 0;

        for (var i = new KeySpliterator<>(sequenceRoot, SequencedElement::getElement, 0, 0); i.moveNext(); ) {
            K e = i.current();
            SequencedElement<K> newElement = new SequencedElement<>(e, seq);
            newRoot = newRoot.update(mutator,
                    newElement,
                    Objects.hashCode(e), 0, details,
                    (oldk, newk) -> oldk.getSequenceNumber() == newk.getSequenceNumber() ? oldk : newk,
                    equalsFunction, hashFunction);
            seq++;
        }
        return newRoot;
    }
}
