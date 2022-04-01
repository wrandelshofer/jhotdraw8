/*
 * @(#)IntEnumerator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * An object for enumerating elements of a collection.
 * <p>
 * The protocol for accessing elements via a {@code Enumerator} imposes smaller per-element overhead than
 * {@link Iterator}, and avoids the inherent race involved in having separate methods for
 * {@code hasNext()} and {@code next()}.
 *
 * @author Werner Randelshofer
 */
public interface IntEnumerator extends Enumerator<Integer>, Spliterator.OfInt {

    /**
     * @return current
     * @see Enumerator#current()
     */
    int currentAsInt();

    @Override
    default boolean tryAdvance(IntConsumer action) {
        if (moveNext()) {
            action.accept(currentAsInt());
            return true;
        }
        return false;
    }

    @Override
    default boolean tryAdvance(Consumer<? super Integer> action) {
        return Enumerator.super.tryAdvance(action);
    }

}
