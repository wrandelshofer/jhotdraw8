/*
 * @(#)NullableUriStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.UriCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.io.Serial;
import java.net.URI;

/**
 * NullableUriStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableUriStyleableKey extends AbstractStyleableKey<URI> implements WritableStyleableMapAccessor<URI> {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Converter<URI> converter = new UriCssConverter(true);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableUriStyleableKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public NullableUriStyleableKey(String key, URI defaultValue) {
        super(key, URI.class, defaultValue);
    }

    @Override
    public @NonNull Converter<URI> getCssConverter() {
        return converter;
    }
}
