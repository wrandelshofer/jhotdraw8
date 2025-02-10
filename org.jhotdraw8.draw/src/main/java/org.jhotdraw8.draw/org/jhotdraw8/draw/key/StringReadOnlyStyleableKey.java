/*
 * @(#)StringReadOnlyStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;

/**
 * StringStyleableKey.
 * <p>
 * XXX - A key should not define whether the user can edit the property in an inspector or not.
 *
 */
public class StringReadOnlyStyleableKey extends AbstractStyleableKey<String> implements ReadOnlyStyleableMapAccessor<String> {

    private static final long serialVersionUID = 1L;
    private final StringCssConverter converter;

    /**
     * Creates a new instance with the specified name and with an empty String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public StringReadOnlyStyleableKey(String name) {
        this(name, "");
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public StringReadOnlyStyleableKey(String name, String defaultValue) {
        this(name, defaultValue, null);
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     * @param helpText     the help text
     */
    public StringReadOnlyStyleableKey(String name, String defaultValue, String helpText) {
        super(null, name, String.class, true, defaultValue);
        converter = new StringCssConverter(false, '\'', helpText);
    }

    @Override
    public Converter<String> getCssConverter() {
        return converter;
    }
}
