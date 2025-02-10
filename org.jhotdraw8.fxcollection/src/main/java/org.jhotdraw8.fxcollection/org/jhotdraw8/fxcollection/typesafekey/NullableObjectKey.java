/*
 * @(#)NullableObjectKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * A simple {@link Key} which has a nullable value.
 *
 * @param <T> the value type
 */
public class NullableObjectKey<T> extends AbstractKey<T> implements NullableKey<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name, type token class, and
     * with null as the default value.
     *
     * @param name The name of the name.
     * @param type The type of the value.
     */
    public NullableObjectKey(String name, Type type) {
        super(name, type, true, false, null);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the name.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public NullableObjectKey(String name, Type type, @Nullable T defaultValue) {
        super(name, type, true, false, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, and
     * with null as the default value.
     *
     * @param name The name of the key.
     * @param type The type of the value.
     */
    public NullableObjectKey(String name, TypeToken<T> type) {
        this(name, type.getType(), null);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the key.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public NullableObjectKey(String name, TypeToken<T> type, @Nullable T defaultValue) {
        this(name, type.getType(), defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, and
     * with null as the default value.
     *
     * @param name  The name of the key.
     * @param clazz The type of the value.
     */
    public NullableObjectKey(String name, Class<?> clazz) {
        super(name, clazz, true, false, null);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the key.
     * @param clazz        The type of the value.
     * @param defaultValue The default value.
     */
    public NullableObjectKey(String name, Class<?> clazz, @Nullable T defaultValue) {
        super(name, clazz, true, false, defaultValue);
    }


}
