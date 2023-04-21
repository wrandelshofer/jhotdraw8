/*
 * @(#)NullableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.Map;

/**
 * NullableKey.
 *
 * @param <T> the value type
 */
public interface NullableKey<T> extends Key<T> {
    @Override
    default @Nullable T get(@NonNull Map<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.getOrDefault(this, getDefaultValue());
        return result;
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    @Override
    default @Nullable T get(@NonNull ReadOnlyMap<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.getOrDefault(this, getDefaultValue());
        return result;
    }
}
