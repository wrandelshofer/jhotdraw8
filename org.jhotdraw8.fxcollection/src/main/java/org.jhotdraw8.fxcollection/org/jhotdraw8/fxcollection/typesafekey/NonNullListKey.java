/*
 * @(#)NonNullListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.lang.reflect.Type;

/**
 * A {@link Key} that stores a list of values.
 *
 * @param <E> the element type of the list
 * @author Werner Randelshofer
 */
public class NonNullListKey<E> extends NonNullObjectKey<ImmutableList<E>> {


    public NonNullListKey(@NonNull String key, @NonNull Type elementType) {
        super(key, new SimpleParameterizedType(ImmutableList.class, elementType), VectorList.of());
    }

    public NonNullListKey(@NonNull String key, @NonNull Type elementType, @NonNull ImmutableList<E> defaultValue) {
        super(key, new SimpleParameterizedType(ImmutableList.class, elementType), defaultValue);
    }
}
