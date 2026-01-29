/*
 * @(#)StringReadableStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadableStyleableMapAccessor;

/**
 * StringStyleableKey.
 *
 */
public class StringReadableStyleableKey extends AbstractStyleableKey<String> implements ReadableStyleableMapAccessor<String> {

    private static final long serialVersionUID = 1L;
    private final StringCssConverter converter;

    /**
     * Creates a new instance with the specified name and with an empty String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public StringReadableStyleableKey(String name) {
        this(name, "");
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public StringReadableStyleableKey(String name, String defaultValue) {
        this(name, defaultValue, null);
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     * @param helpText     the help text
     */
    public StringReadableStyleableKey(String name, String defaultValue, String helpText) {
        super(null, name, String.class, true, defaultValue);
        converter = new StringCssConverter(false, '\'', helpText);
    }

    @Override
    public Converter<String> getCssConverter() {
        return converter;
    }
}
