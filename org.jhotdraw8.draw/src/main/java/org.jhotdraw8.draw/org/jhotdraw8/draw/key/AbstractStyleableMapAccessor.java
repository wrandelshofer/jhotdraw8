/*
 * @(#)AbstractStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.CompositeMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractStyleableMapAccessor.
 *
 * @param <T> the value type
 */
public abstract class AbstractStyleableMapAccessor<T>
        implements WritableStyleableMapAccessor<T>, CompositeMapAccessor<T> {

    private final String cssName;


    /**
     * Holds a String representation of the name.
     */
    private final String name;
    /**
     * Holds the default value.
     */
    private final @Nullable T defaultValue;
    /**
     * This variable is used as a "type token" so that we can check for
     * assignability of attribute values at runtime.
     */
    private final Type type;

    private final PersistentSequencedSet<MapAccessor<?>> subAccessors;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name         The name of the key.
     * @param type         The type of the value.
     * @param subAccessors sub accessors which are used by this accessor
     * @param defaultValue The default value.
     */
    public AbstractStyleableMapAccessor(String name, Class<T> type, MapAccessor<?>[] subAccessors, T defaultValue) {
        this(name, type, null, subAccessors, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name           The name of the key.
     * @param type           The type of the value.
     * @param typeParameters The type parameters of the class. Specify "" if no
     *                       type parameters are given. Otherwise, specify them in arrow brackets.
     * @param subAccessors   sub accessors which are used by this accessor
     * @param defaultValue   The default value.
     */
    public AbstractStyleableMapAccessor(String name, Class<?> type, @Nullable Class<?>[] typeParameters,
                                        MapAccessor<?>[] subAccessors, @Nullable T defaultValue) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "clazz");

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;

        this.subAccessors = ChampVectorSet.of(subAccessors);
        cssName = ReadOnlyStyleableMapAccessor.toCssName(name);
    }

    @Override
    public boolean containsKey(Map<Key<?>, Object> map) {
        return CompositeMapAccessor.super.containsKey(map);
    }

    /**
     * Returns the name string.
     *
     * @return name string.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getValueType() {
        return type;
    }


    /**
     * Returns the default value of the attribute.
     *
     * @return the default value.
     */
    @Override
    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final @Nullable T put(Map<? super Key<?>, Object> a, @Nullable T value) {
        T oldValue = get(a);
        set(a, value);
        return oldValue;
    }

    /**
     * Returns the name string.
     */
    @Override
    public String toString() {
        String keyClass = getClass().getName();
        return keyClass.substring(keyClass.lastIndexOf('.') + 1) + "{name:" + name + " type:" + getValueType() + "}";
    }

    @Override
    public PersistentSequencedSet<MapAccessor<?>> getSubAccessors() {
        return subAccessors;
    }


    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public String getCssName() {
        return cssName;
    }
}
