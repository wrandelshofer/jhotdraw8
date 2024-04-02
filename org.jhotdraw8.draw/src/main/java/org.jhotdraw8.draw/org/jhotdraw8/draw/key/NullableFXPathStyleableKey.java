/*
 * @(#)NullableSvgPathStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.shape.Path;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.FXPathCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableSvgPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableFXPathStyleableKey extends AbstractStyleableKey<Path> implements WritableStyleableMapAccessor<Path> {

    private final @NonNull Converter<Path> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableFXPathStyleableKey(@NonNull String name) {
        this(name, null);
    }


    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise,
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    @SuppressWarnings("this-escape")
    public NullableFXPathStyleableKey(@NonNull String key, @Nullable Path defaultValue) {
        super(null, key, Path.class, true, defaultValue);
        converter = new FXPathCssConverter(true);
    }

    @Override
    public @NonNull Converter<Path> getCssConverter() {
        return converter;
    }
}
