/*
 * @(#)AbstractKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A <em>name</em> which provides typesafe access to a map entry.
 * <p>
 * A Key has a name, a type and a default value.
 * <p>
 * The following code example shows how to set and get a value from a map.
 * <pre>
 * {@code
 * String value = "Werner";
 * Key<String> stringKey = new Key("name",String.class,null);
 * Map<Key<?>,Object> map = new HashMap<>();
 * stringKey.put(map, value);
 * }
 * </pre>
 * <p>
 * Note that {@code Key} is not a value type. Thus using two distinct instances
 * of a Key will result in two distinct entries in the hash map, even if both
 * keys have the same name.
 *
 * @param <T> the value type
 */
public abstract class AbstractKey<T> implements Key<T> {


    /**
     * Holds a String representation of the name.
     */
    private final String name;
    /**
     * Holds the default value.
     */
    private final @Nullable T initialValue;
    /**
     * This variable is used as a "type token" so that we can check for
     * assignability of attribute values at runtime.
     */
    private final Type type;


    /**
     * Whether the value may be set to null.
     */
    private final boolean isNullable;
    private final boolean isTransient;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value null, and allowing null values.
     *
     * @param name  The name of the key.
     * @param clazz The type of the value.
     */
    public AbstractKey(String name, Type clazz) {
        this(name, clazz, null);
    }

    public AbstractKey(String name, TypeToken<T> clazz, T initialValue) {
        this(name, clazz.getType(), initialValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name         The name of the key.
     * @param clazz        The type of the value.
     * @param initialValue The default value.
     */
    public AbstractKey(String name, Type clazz, @Nullable T initialValue) {
        this(name, clazz, true, initialValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param name         The name of the key.
     * @param clazz        The type of the value.
     * @param isNullable   Whether the value may be set to null
     * @param initialValue The default value.
     */
    public AbstractKey(String name, Type clazz, boolean isNullable, @Nullable T initialValue) {
        this(name, clazz, isNullable, false, initialValue);
    }

    public AbstractKey(String name, Type clazz, @Nullable Class<?>[] typeParameters, boolean isNullable, @Nullable T initialValue) {
        this(name, clazz, isNullable, false, initialValue);
    }


    public AbstractKey(@Nullable String name, Type clazz, boolean isNullable, boolean isTransient, @Nullable T initialValue) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(clazz, "clazz");
        if (!isNullable && initialValue == null) {
            throw new IllegalArgumentException("defaultValue may not be null if isNullable==false");
        }

        this.name = name;
        this.type = clazz;
        this.isNullable = isNullable;
        this.isTransient = isTransient;
        this.initialValue = initialValue;
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

    @Override
    public @Nullable T getDefaultValue() {
        return initialValue;
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Returns the name string.
     */
    @Override
    public String toString() {
        String keyClass = getClass().getName();
        return keyClass.substring(keyClass.lastIndexOf('.') + 1) + "@" + System.identityHashCode(this) + " {\"" + name + "\"}";
    }
}
