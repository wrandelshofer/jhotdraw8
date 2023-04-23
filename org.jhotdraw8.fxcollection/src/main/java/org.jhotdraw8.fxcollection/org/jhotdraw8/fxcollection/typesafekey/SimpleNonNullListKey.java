/*
 * @(#)SimpleNonNullListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;

import java.io.Serial;

/**
 * A {@link Key} that stores a list of values.
 *
 * @param <E> the element type of the list
 * @author Werner Randelshofer
 */
public class SimpleNonNullListKey<E> extends SimpleNonNullKey<ImmutableList<E>> {

    @Serial
    private static final long serialVersionUID = 1L;

    public SimpleNonNullListKey(@NonNull String key, @NonNull TypeToken<ImmutableList<E>> type) {
        super(key, type, VectorList.of());
    }

    public SimpleNonNullListKey(@NonNull String key, @NonNull TypeToken<ImmutableList<E>> type, @NonNull ImmutableList<E> defaultValue) {
        super(key, type, defaultValue);
    }
}
