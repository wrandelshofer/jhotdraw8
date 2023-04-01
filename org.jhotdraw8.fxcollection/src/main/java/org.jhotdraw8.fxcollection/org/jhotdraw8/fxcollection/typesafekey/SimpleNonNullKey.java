/*
 * @(#)SimpleNonNullKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * A simple {@link Key} which has a non-nullable value.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class SimpleNonNullKey<@NonNull T> extends AbstractKey<@NonNull T> implements
        NonNullKey<@NonNull T> {

    static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the name.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public SimpleNonNullKey(@NonNull String name, @NonNull Type type, @NonNull T defaultValue) {
        super(name, type, false, false, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the key.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public SimpleNonNullKey(@NonNull String name, @NonNull TypeToken<T> type, @NonNull T defaultValue) {
        this(name, type.getType(), defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the key.
     * @param clazz        The type of the value.
     * @param defaultValue The default value.
     */
    public SimpleNonNullKey(@NonNull String name, @NonNull Class<?> clazz, @NonNull T defaultValue) {
        super(name, clazz, true, false, defaultValue);
    }
}
