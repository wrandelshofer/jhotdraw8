/*
 * @(#)NullableFXPathElementsStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.FXPathElementsCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

/**
 * NullableFXSvgPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableFXPathElementsStyleableKey extends AbstractStyleableKey<ImmutableList<PathElement>> implements WritableStyleableMapAccessor<ImmutableList<PathElement>> {


    private final Converter<ImmutableList<PathElement>> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableFXPathElementsStyleableKey(String name) {
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
    @SuppressWarnings("this-escape")
    public NullableFXPathElementsStyleableKey(String key, @Nullable ImmutableList<PathElement> defaultValue) {
        super(null, key, new SimpleParameterizedType(ImmutableList.class, PathElement.class), true, defaultValue);

        converter = new FXPathElementsCssConverter(isNullable());
    }

    @Override
    public Converter<ImmutableList<PathElement>> getCssConverter() {
        return converter;
    }
}
