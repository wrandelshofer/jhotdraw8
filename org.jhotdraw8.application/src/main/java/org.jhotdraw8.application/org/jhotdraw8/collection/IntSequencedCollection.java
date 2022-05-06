/*
 * @(#)IntSequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Interface for collection of {@code int}-values with a well-defined linear
 * ordering of its elements.
 */
public interface IntSequencedCollection extends SequencedCollection<Integer> {
    void addFirstAsInt(int e);

    void addLastAsInt(int e);

    int getFirstAsInt();

    int getLastAsInt();

    int removeFirstAsInt();

    int removeLastAsInt();

    @Override
    default void addFirst(Integer e) {
        addFirstAsInt(e);
    }

    @Override
    default void addLast(Integer e) {
        addLastAsInt(e);
    }

    @Override
    default Integer getFirst() {
        return getFirstAsInt();
    }

    @Override
    default Integer getLast() {
        return getLastAsInt();
    }

    @Override
    default Integer removeFirst() {
        return removeFirstAsInt();
    }

    @Override
    default Integer removeLast() {
        return removeLastAsInt();
    }

    default void addLastAllAsInt(int @NonNull [] array) {
        addLastAllAsInt(array, 0, array.length);
    }

    default void addLastAllAsInt(int @NonNull [] array, int offset, int length) {
        for (int i = offset, limit = offset + length; i < limit; i++) {
            addLastAsInt(array[i]);
        }
    }
}