/*
 * @(#)StringOrIdentStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.CssStringOrIdentConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

/**
 * This key has a string value which can be given as a CSS "IDENT"-token or
 * as a CSS "STRING"-token.
 *
 * @author Werner Randelshofer
 */
public class StringOrIdentStyleableKey extends AbstractStyleableKey<@NonNull String>
        implements WritableStyleableMapAccessor<@NonNull String>, NonNullMapAccessor<@NonNull String> {

    static final long serialVersionUID = 1L;
    private final Converter<String> converter = new CssStringOrIdentConverter();

    /**
     * Creates a new instance with the specified name and with an empty String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public StringOrIdentStyleableKey(@NonNull String name) {
        this(name, "");
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public StringOrIdentStyleableKey(@NonNull String name, String defaultValue) {
        super(null, name, String.class, false, defaultValue);
    }

    @Override
    public @NonNull Converter<String> getCssConverter() {
        return converter;
    }
}
