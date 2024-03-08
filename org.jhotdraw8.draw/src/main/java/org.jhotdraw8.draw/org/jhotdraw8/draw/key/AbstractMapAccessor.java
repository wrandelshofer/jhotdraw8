/*
 * @(#)AbstractMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxcollection.typesafekey.CompositeMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * AbstractMapAccessor.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public abstract class AbstractMapAccessor<T> implements CompositeMapAccessor<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Holds a String representation of the name.
     */
    private final @NonNull String name;
    /**
     * Holds the default value.
     */
    private final @Nullable T defaultValue;
    /**
     * This variable is used as a "type token" so that we can check for
     * assignability of attribute values at runtime.
     */
    private final @NonNull Type type;

    private final @NonNull ImmutableSequencedSet<MapAccessor<?>> subAccessors;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name         The name of the key.
     * @param type         The type of the value.
     * @param subAccessors sub accessors which are used by this accessor
     * @param defaultValue The default value.
     */
    public AbstractMapAccessor(@NonNull String name, Class<T> type, @NonNull MapAccessor<?>[] subAccessors, T defaultValue) {
        this(name, type, null, subAccessors, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name           The name of the key.
     * @param type           The type of the value.
     * @param typeParameters The type parameters of the class. Specify "" if no
     *                       type parameters are given. Otherwise specify them in arrow brackets.
     * @param subAccessors   sub accessors which are used by this accessor
     * @param defaultValue   The default value.
     */
    public AbstractMapAccessor(@NonNull String name, @Nullable Class<?> type, @Nullable Class<?>[] typeParameters, @NonNull MapAccessor<?>[] subAccessors, @Nullable T defaultValue) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "clazz");
        Objects.requireNonNull(defaultValue, "defaultValue");

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.subAccessors = ChampVectorSet.of(subAccessors);
    }

    /**
     * Returns the name string.
     *
     * @return name string.
     */
    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull Type getValueType() {
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

    /**
     * Returns the name string.
     */
    @Override
    public @NonNull String toString() {
        String keyClass = getClass().getName();
        return keyClass.substring(keyClass.lastIndexOf('.') + 1) + "{name:" + name + " type:" + getValueType() + "}";
    }

    @Override
    public @NonNull ImmutableSequencedSet<MapAccessor<?>> getSubAccessors() {
        return subAccessors;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

}
