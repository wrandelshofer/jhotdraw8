/*
 * @(#)SimpleNonNullListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * A {@link Key} that stores a list of values.
 *
 * @param <E> the element type of the list
 * @author Werner Randelshofer
 */
public class SimpleNonNullListKey<E> extends SimpleNonNullKey<ImmutableList<E>> {

    @Serial
    private static final long serialVersionUID = 1L;

    public SimpleNonNullListKey(@NonNull String key, @NonNull Type elementType) {
        super(key, new SimpleParameterizedType(ImmutableList.class, elementType), SimpleImmutableList.of());
    }

    public SimpleNonNullListKey(@NonNull String key, @NonNull Type elementType, @NonNull ImmutableList<E> defaultValue) {
        super(key, new SimpleParameterizedType(ImmutableList.class, elementType), defaultValue);
    }
}
