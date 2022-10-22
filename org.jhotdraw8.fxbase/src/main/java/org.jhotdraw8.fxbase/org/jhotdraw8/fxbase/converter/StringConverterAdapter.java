/*
 * @(#)StringConverterAdapter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.converter;

import javafx.util.StringConverter;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;

import java.io.IOException;
import java.text.ParseException;

/**
 * Allows to use a {@code Converter} with the
 * {@code javafx.util.StringConverter} API.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class StringConverterAdapter<T> extends StringConverter<T> {

    private final Converter<T> converter;

    public StringConverterAdapter(Converter<T> converter) {
        this.converter = converter;
    }

    @Override
    public @NonNull String toString(T object) {
        return converter.toString(object);
    }

    @Override
    public @Nullable T fromString(@NonNull String string) {
        try {
            return converter.fromString(string);
        } catch (ParseException | IOException ex) {
            return converter.getDefaultValue();
        }
    }

}
