/*
 * @(#)DefaultConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;

/// Converts an `Object` to a `String` but can not a `String`
/// back to an `Object`.
///
/// This converter is not bijective, and thus only useful for one-way conversions
/// to a String. For example for generating a message text.
///
///   - The conversion to string is performed by invoking the `toString`
///     method on the value object.
///   - The `fromString` method always returns a String object regardless
///     of the type that was converted `toString`.
///   - If a null value is converted to string, the string contains the text
///     `"null"`. On the conversion from string, a String object with the value
///     `"null"` is returned.
///
public class DefaultConverter implements Converter<Object> {

    public DefaultConverter() {
    }

    @Override
    public @Nullable Object fromString(CharBuffer buf, @Nullable IdResolver idResolver) {
        String str = buf.toString();
        buf.position(buf.limit());
        return "null".equals(str) ? null : str;
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable Object value) throws IOException {
        out.append(value == null ? "null" : value.toString());
    }

    @Override
    public @Nullable String getDefaultValue() {
        return "null";
    }
}
