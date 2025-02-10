/*
 * @(#)MapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A type safe accessor for maps.
 * <p>
 * Design pattern: Strategy
 *
 * @param <T> The value type.
 */
public interface MapAccessor<T> {

    /**
     * Whether the map contains all keys required by this map accessor.
     *
     * @param map a map
     * @return true if map contains all keys required by this map accessor.
     */
    boolean containsKey(Map<Key<?>, Object> map);

    /**
     * Returns the name string.
     *
     * @return name string.
     */
    String getName();

    /**
     * Gets the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    @Nullable
    T get(Map<? super Key<?>, Object> a);

    default @Nullable T get(ReadableMap<? super Key<?>, Object> a) {
        return get(a.asMap());
    }

    /**
     * Puts the value of the attribute denoted by this accessor from a Map.
     *
     * @param a     A map.
     * @param value The new value. Subclasses may require that the value is non-null.
     * @return The old value.
     */
    @Nullable
    T put(Map<? super Key<?>, Object> a, @Nullable T value);

    /**
     * Puts the value of the attribute denoted by this accessor from a Map.
     *
     * @param a     A map.
     * @param value The new value. Subclasses may require that the value is non-null.
     * @return The updated map.
     */
    PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable T value);

    /**
     * Sets the value of the attribute denoted by this accessor from a Map.
     *
     * @param a     A map.
     * @param value The new value. Subclasses may require that the value is non-null.
     */
    default void set(Map<? super Key<?>, Object> a, @Nullable T value) {
        put(a, value);
    }

    /**
     * Removes the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A map.
     * @return The old value.
     */
    @Nullable
    T remove(Map<? super Key<?>, Object> a);

    /**
     * Removes the value of the attribute denoted by this accessor from a Map.
     *
     * @param a A map.
     * @return The old value.
     */
    PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a);

    /**
     * Returns the value type of this map accessor.
     * <p>
     * If the value type has type parameters, make sure to create it using
     * {@link TypeToken}.
     */
    Type getValueType();

    /**
     * Returns the raw value type of this map accessor.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getRawValueType() {
        Type t = getValueType();
        return (Class<T>) ((t instanceof ParameterizedType) ? ((ParameterizedType) t).getRawType() : t);
    }

    /**
     * Returns the default value of this map accessor.
     * <p>
     * The default value of an attribute or property is
     * the value used when that attribute or property is not
     * specified.
     *
     * @return the default value
     */
    @Nullable T getDefaultValue();

    /**
     * Whether the value needs to be made persistent.
     *
     * @return true if transient
     */
    boolean isTransient();

}
