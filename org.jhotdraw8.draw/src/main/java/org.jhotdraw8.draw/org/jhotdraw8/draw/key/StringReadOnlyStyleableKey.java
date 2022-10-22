/*
 * @(#)StringReadOnlyStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.text.CssStringConverter;
import org.jhotdraw8.styleable.ReadOnlyStyleableMapAccessor;

/**
 * StringStyleableKey.
 * <p>
 * XXX - A key should not define whether the user can edit the property in an inspector or not.
 *
 * @author Werner Randelshofer
 */
public class StringReadOnlyStyleableKey extends AbstractStyleableKey<String> implements ReadOnlyStyleableMapAccessor<String> {

    static final long serialVersionUID = 1L;
    private final @NonNull CssStringConverter converter;

    /**
     * Creates a new instance with the specified name and with an empty String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public StringReadOnlyStyleableKey(@NonNull String name) {
        this(name, "");
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public StringReadOnlyStyleableKey(@NonNull String name, String defaultValue) {
        this(name, defaultValue, null);
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     * @param helpText     the help text
     */
    public StringReadOnlyStyleableKey(@NonNull String name, String defaultValue, String helpText) {
        super(null, name, String.class, true, defaultValue);
        converter = new CssStringConverter(false, '\'', helpText);
    }

    @Override
    public @NonNull Converter<String> getCssConverter() {
        return converter;
    }
}
