/*
 * @(#)StringStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.styleable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssStringConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;

public class StringStyleableKey extends SimpleNullableKey<String> implements WritableStyleableMapAccessor<String> {
    private static final long serialVersionUID = 0L;
    private final @NonNull String cssName;
    private final CssStringConverter converter = new CssStringConverter();

    public StringStyleableKey(String key) {
        this(key, null);
    }

    public StringStyleableKey(String key, String defaultValue) {
        super(key, String.class, defaultValue);
        cssName = ReadOnlyStyleableMapAccessor.toCssName(getName());
    }

    @Override
    public @NonNull Converter<String> getCssConverter() {
        return converter;
    }

    @Override
    public @NonNull String getCssName() {
        return cssName;
    }
}
