/*
 * @(#)StyleConverterAdapter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.text;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;

import java.io.IOException;
import java.text.ParseException;

/**
 * Allows to use a {@link Converter} with the {@code javafx.css.StyleConverter}
 * API.
 *
 * @author Werner Randelshofer
 * rawcoder $
 */
public class StyleConverterAdapter<T> extends StyleConverter<String, T> {

    private final Converter<T> converter;

    public StyleConverterAdapter(Converter<T> converter) {
        this.converter = converter;
    }

    @Override
    public @Nullable T convert(@NonNull ParsedValue<String, T> value, Font font) {
        try {
            return converter.fromString(value.getValue());
        } catch (ParseException | IOException ex) {
            return converter.getDefaultValue();
        }
    }
}
