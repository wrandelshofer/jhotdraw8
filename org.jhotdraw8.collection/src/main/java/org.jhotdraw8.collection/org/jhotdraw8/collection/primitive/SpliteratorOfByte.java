/*
 * @(#)SpliteratorOfByte.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Spliterator;
import java.util.function.Consumer;

public interface SpliteratorOfByte extends Spliterator.OfPrimitive<Byte, ByteConsumer, SpliteratorOfByte> {

    @Override
    @Nullable SpliteratorOfByte trySplit();

    @Override
    boolean tryAdvance(@NonNull ByteConsumer action);

    @Override
    default void forEachRemaining(@NonNull ByteConsumer action) {
        do {
        } while (tryAdvance(action));
    }

    @Override
    default boolean tryAdvance(@NonNull Consumer<? super Byte> action) {
        if (action instanceof ByteConsumer) {
            return tryAdvance((ByteConsumer) action);
        } else {
            return tryAdvance((ByteConsumer) action::accept);
        }
    }

    @Override
    default void forEachRemaining(@NonNull Consumer<? super Byte> action) {
        if (action instanceof ByteConsumer) {
            forEachRemaining((ByteConsumer) action);
        } else {
            forEachRemaining((ByteConsumer) action::accept);
        }
    }
}
