/*
 * @(#)NonNullMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;
import java.util.Objects;

/**
 * NonNullMapAccessor.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public interface NonNullMapAccessor<T> extends MapAccessor<T> {
    long serialVersionUID = 1L;

    /**
     * Gets the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    default T getNonNull(Map<? super Key<?>, Object> a) {
        T t = get(a);
        assert t != null;
        return t;
    }

    /**
     * Gets the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    default T getNonNull(ReadableMap<? super Key<?>, Object> a) {
        T t = get(a);
        assert t != null;
        return t;
    }

    /**
     * Puts the value of the attribute denoted by this accessor from a Map.
     *
     * @param a     A map.
     * @param value The new value.
     * @return The old value.
     */
    default T putNonNull(Map<? super Key<?>, Object> a, T value) {
        T t = put(a, value);
        assert t != null;
        return t;
    }

    default T getDefaultValueNonNull() {
        T v = getDefaultValue();
        return Objects.requireNonNull(v, "default value of " + getName() + " must not be null.");
    }
}
