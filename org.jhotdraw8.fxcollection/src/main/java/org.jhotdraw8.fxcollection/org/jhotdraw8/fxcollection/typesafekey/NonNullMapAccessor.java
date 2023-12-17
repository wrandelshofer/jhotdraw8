/*
 * @(#)NonNullMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;
import java.util.Objects;

/**
 * NonNullMapAccessor.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public interface NonNullMapAccessor<@NonNull T> extends MapAccessor<T> {
    long serialVersionUID = 1L;

    /**
     * Gets the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    default @NonNull T getNonNull(@NonNull Map<? super Key<?>, Object> a) {
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
    default @NonNull T getNonNull(@NonNull ReadOnlyMap<? super Key<?>, Object> a) {
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
    default @NonNull T putNonNull(@NonNull Map<? super Key<?>, Object> a, @NonNull T value) {
        T t = put(a, value);
        assert t != null;
        return t;
    }

    default @NonNull T getDefaultValueNonNull() {
        T v = getDefaultValue();
        return Objects.requireNonNull(v, "default value of " + getName() + " must not be null.");
    }
}
