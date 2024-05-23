/*
 * @(#)StringOrIdentStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.StringOrIdentCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * This key has a string value which can be given as a CSS "IDENT"-token or
 * as a CSS "STRING"-token.
 *
 * @author Werner Randelshofer
 */
public class StringOrIdentStyleableKey extends AbstractStyleableKey<String>
        implements WritableStyleableMapAccessor<String>, NonNullKey<String> {

    private static final long serialVersionUID = 1L;
    private final Converter<String> converter = new StringOrIdentCssConverter();

    /**
     * Creates a new instance with the specified name and with an empty String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public StringOrIdentStyleableKey(String name) {
        this(name, "");
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public StringOrIdentStyleableKey(String name, String defaultValue) {
        super(null, name, String.class, false, defaultValue);
    }

    @Override
    public Converter<String> getCssConverter() {
        return converter;
    }
}
