/*
 * @(#)NonNullKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;

/**
 * NonNullKey.
 *
 * @param <T> the value type
 */
public interface NonNullKey<T> extends Key<T>, NonNullMapAccessor<T> {
    @Override
    default T get(Map<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.get(this);
        return result == null ? getDefaultValueNonNull() : result;
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    @Override
    default T get(ReadOnlyMap<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.get(this);
        return result == null ? getDefaultValueNonNull() : result;
    }
}
