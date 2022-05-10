/*
 * @(#)SequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface for a collection with a well-defined linear ordering of its elements.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedCollection<E> extends Collection<E>, ReadOnlySequencedCollection<E> {

    @Override
    default boolean isEmpty() {
        return ReadOnlySequencedCollection.super.isEmpty();
    }

    @Override
    default <T> T @NonNull [] toArray(T @NonNull [] a) {
        return ReadOnlySequencedCollection.super.toArray(a);
    }

    @Override
    default Object @NonNull [] toArray() {
        return ReadOnlySequencedCollection.super.toArray();
    }

    @Override
    default Stream<E> stream() {
        return Collection.super.stream();
    }
}
