/*
 * @(#)NonNullObjectStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxbase.styleable.ReadableStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.lang.reflect.Type;

/**
 * A simple non-nullable StyleableKey.
 *
 * @param <T> the value type
 */
public class NonNullObjectStyleableKey<T> extends AbstractReadableStyleableKey<T> implements WritableStyleableMapAccessor<T>,
        NonNullKey<T> {
    private final PersistentList<String> examples;

    /**
     * Creates a new instance.
     *
     * @param name         The name of the key
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NonNullObjectStyleableKey(String name, Type type, Converter<T> converter, T defaultValue) {
        this(name, ReadableStyleableMapAccessor.toCssName(name), type, converter, defaultValue);
    }

    /**
     * Creates a new instance.
     *
     * @param xmlName      The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NonNullObjectStyleableKey(String xmlName, String cssName, Type type, Converter<T> converter, T defaultValue) {
        this(xmlName, cssName, type, converter, defaultValue, VectorList.of());
    }

    /**
     * Creates a new instance.
     *
     * @param name         The XML name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     * @param examples     Examples
     */
    public NonNullObjectStyleableKey(String name, Type type, Converter<T> converter, T defaultValue, PersistentList<String> examples) {
        super(name, ReadableStyleableMapAccessor.toCssName(name), type, converter, defaultValue);
        this.examples = examples;
    }

    /**
     * Creates a new instance.
     *
     * @param xmlName      The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     * @param examples     Examples
     */
    public NonNullObjectStyleableKey(String xmlName, String cssName, Type type, Converter<T> converter, T defaultValue, PersistentList<String> examples) {
        super(xmlName, cssName, type, converter, defaultValue);
        this.examples = examples;
    }

    @Override
    public PersistentList<String> getExamples() {
        return examples;
    }
}
