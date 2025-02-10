/*
 * @(#)NullableStringStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * NullableStringStyleableKey.
 *
 */
public class NullableStringStyleableKey extends AbstractStyleableKey<String>
        implements WritableStyleableMapAccessor<String> {

    private static final long serialVersionUID = 1L;
    private final StringCssConverter converter;

    /**
     * Creates a new instance with the specified name and with a null String
     * as the default value.
     *
     * @param name The name of the key.
     */
    public NullableStringStyleableKey(String name) {
        this(null, name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name The name of the key.
     */
    public NullableStringStyleableKey(String namespace, String name) {
        this(namespace, name, null);
    }

    public NullableStringStyleableKey(String namespace, String name, @Nullable String helpText) {
        super(namespace, name, String.class, true, null);
        converter = new StringCssConverter(true, '\'', helpText);
    }

    @Override
    public Converter<String> getCssConverter() {
        return converter;
    }
}
