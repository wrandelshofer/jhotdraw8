/*
 * @(#)DoubleSequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;

import java.util.SequencedCollection;

/**
 * Interface for collection of {@code int}-values with a well-defined linear
 * ordering of its elements.
 */
public interface DoubleSequencedCollection extends SequencedCollection<Double> {
    void addFirstAsDouble(double e);

    void addLastAsDouble(double e);

    double getFirstAsDouble();

    double getLastAsDouble();

    double removeFirstAsDouble();

    double removeLastAsDouble();

    default void addFirst(Double e) {
        addFirstAsDouble(e);
    }

    default void addLast(Double e) {
        addLastAsDouble(e);
    }

    @Override
    default Double getFirst() {
        return getFirstAsDouble();
    }

    @Override
    default Double getLast() {
        return getLastAsDouble();
    }

    default Double removeFirst() {
        return removeFirstAsDouble();
    }

    default Double removeLast() {
        return removeLastAsDouble();
    }

    default void addLastAllAsDouble(double @NonNull [] array) {
        addLastAllAsDouble(array, 0, array.length);
    }

    default void addLastAllAsDouble(double @NonNull [] array, int offset, int length) {
        for (int i = offset, limit = offset + length; i < limit; i++) {
            addLastAsDouble(array[i]);
        }
    }
}