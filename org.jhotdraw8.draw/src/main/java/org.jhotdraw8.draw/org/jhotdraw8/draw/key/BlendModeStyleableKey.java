/*
 * @(#)BlendModeStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.effect.BlendMode;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssKebabCaseEnumConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * BlendModeStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class BlendModeStyleableKey extends AbstractStyleableKey<BlendMode> implements WritableStyleableMapAccessor<BlendMode>, NonNullKey<BlendMode> {

    static final long serialVersionUID = 1L;
    private final @NonNull Converter<BlendMode> converter = new CssKebabCaseEnumConverter<>(BlendMode.class, false);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public BlendModeStyleableKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public BlendModeStyleableKey(@NonNull String name, BlendMode defaultValue) {
        super(name, BlendMode.class, defaultValue);
    }

    @Override
    public @NonNull Converter<BlendMode> getCssConverter() {
        return converter;
    }

}
