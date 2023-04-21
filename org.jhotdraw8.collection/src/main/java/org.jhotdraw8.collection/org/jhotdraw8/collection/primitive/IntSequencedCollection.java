/*
 * @(#)IntSequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

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

    default void addFirst(Integer e) {
        addFirstAsInt(e);
    }

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

    default Integer removeFirst() {
        return removeFirstAsInt();
    }

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