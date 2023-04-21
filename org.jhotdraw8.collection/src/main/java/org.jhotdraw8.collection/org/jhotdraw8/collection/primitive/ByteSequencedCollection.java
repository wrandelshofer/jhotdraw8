/*
 * @(#)ByteSequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

/**
 * Interface for collection of {@code int}-values with a well-defined linear
 * ordering of its elements.
 */
public interface ByteSequencedCollection extends SequencedCollection<Byte> {
    void addFirstAsByte(byte e);

    void addLastAsByte(byte e);

    byte getFirstAsByte();

    byte getLastAsByte();

    byte removeFirstAsByte();

    byte removeLastAsByte();

    default void addFirst(Byte e) {
        addFirstAsByte(e);
    }

    default void addLast(Byte e) {
        addLastAsByte(e);
    }

    @Override
    default Byte getFirst() {
        return getFirstAsByte();
    }

    @Override
    default Byte getLast() {
        return getLastAsByte();
    }

    default Byte removeFirst() {
        return removeFirstAsByte();
    }

    default Byte removeLast() {
        return removeLastAsByte();
    }

    default void addLastAllAsByte(byte @NonNull [] array) {
        addLastAllAsByte(array, 0, array.length);
    }

    default void addLastAllAsByte(byte @NonNull [] array, int offset, int length) {
        for (int i = offset, limit = offset + length; i < limit; i++) {
            addLastAsByte(array[i]);
        }
    }
}