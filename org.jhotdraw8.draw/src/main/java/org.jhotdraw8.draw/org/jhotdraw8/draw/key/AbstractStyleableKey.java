/*
 * @(#)AbstractStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.AbstractKey;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * AbstractStyleableKey.
 *
 * @param <T> the value type
 */
public abstract class AbstractStyleableKey<T> extends AbstractKey<T> implements ReadOnlyStyleableMapAccessor<T> {

    private static final long serialVersionUID = 1L;
    private final String cssName;
    private final @Nullable String namespace;


    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param key          The name of the name.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public AbstractStyleableKey(String key, Type type, @Nullable T defaultValue) {
        this(null, key, ReadOnlyStyleableMapAccessor.toCssName(key), type, defaultValue == null, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param namespace    The namespace
     * @param name         The name of the key.
     * @param type         The type of the value.
     * @param isNullable   Whether the value may be set to null
     * @param defaultValue The default value.
     */
    public AbstractStyleableKey(@Nullable String namespace, String name, Type type, boolean isNullable, @Nullable T defaultValue) {
        this(namespace, name, ReadOnlyStyleableMapAccessor.toCssName(name), type, isNullable, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param namespace    The namespace
     * @param name         The name of the key.
     * @param cssName      The name of the as seen by CSS.
     * @param type         The type of the value.
     * @param isNullable   Whether the value may be set to null
     * @param defaultValue The default value.
     */
    public AbstractStyleableKey(@Nullable String namespace, String name, String cssName, Type type, boolean isNullable, @Nullable T defaultValue) {
        super(name, type, isNullable, defaultValue);
        this.cssName = cssName;
        this.namespace = namespace;
    }

    @Override
    public String getCssName() {
        return cssName;
    }

    @Override
    public @Nullable String getCssNamespace() {
        return namespace;
    }
}
