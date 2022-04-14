/*
 * @(#)IntSequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A collection with a well-defined linear ordering of its elements.
 *
 * @param <E> the element type
 */
public interface SequencedCollection<E> extends Collection<E>, ReadOnlySequencedCollection<E> {
    void addFirst(E e);

    void addLast(E e);

    E removeFirst();

    E removeLast();

    @Override
    default boolean isEmpty() {
        return ReadOnlySequencedCollection.super.isEmpty();
    }

    @Override
    default <T> T[] toArray(T @NonNull [] a) {
        return ReadOnlySequencedCollection.super.toArray(a);
    }

    @Override
    default Object[] toArray() {
        return ReadOnlySequencedCollection.super.toArray();
    }

    @Override
    default Stream<E> stream() {
        return Collection.super.stream();
    }
}
