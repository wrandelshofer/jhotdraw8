/*
 * @(#)PrimitiveIteratorOfByte.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

public interface PrimitiveIteratorOfByte extends PrimitiveIterator<Byte, ByteConsumer> {

    byte nextByte();

    @Override
    default void forEachRemaining(@NonNull ByteConsumer action) {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.accept(nextByte());
        }
    }

    @Override
    default Byte next() {
        return nextByte();
    }

    @Override
    default void forEachRemaining(@NonNull Consumer<? super Byte> action) {
        if (action instanceof ByteConsumer) {
            forEachRemaining((ByteConsumer) action);
        } else {
            // The method reference action::accept is never null
            Objects.requireNonNull(action);
            forEachRemaining((ByteConsumer) action::accept);
        }
    }
}
