/*
 * @(#)NullableStringStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssStringConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableStringStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableStringStyleableKey extends AbstractStyleableKey<String>
        implements WritableStyleableMapAccessor<String> {

    static final long serialVersionUID = 1L;
    private final @NonNull CssStringConverter converter;

    /**
     * Creates a new instance with the specified name and with a null String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public NullableStringStyleableKey(@NonNull String name) {
        this(null, name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name The name of the key.
     */
    public NullableStringStyleableKey(String namespace, @NonNull String name) {
        this(namespace, name, null);
    }

    public NullableStringStyleableKey(String namespace, @NonNull String name, String helpText) {
        super(namespace, name, String.class, true, null);
        converter = new CssStringConverter(true, '\'', helpText);
    }

    @Override
    public @NonNull Converter<String> getCssConverter() {
        return converter;
    }
}
