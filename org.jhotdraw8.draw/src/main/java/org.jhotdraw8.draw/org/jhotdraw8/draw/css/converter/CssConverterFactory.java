/*
 * @(#)CssConverterFactory.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.ConverterFactory;
import org.jhotdraw8.base.converter.DefaultConverter;

/**
 * CssConverterFactory.
 *
 * @author Werner Randelshofer
 */
public class CssConverterFactory implements ConverterFactory {

    public CssConverterFactory() {
    }

    @Override
    public @NonNull Converter<?> apply(@Nullable String type, String style) {
        if (type == null) {
            return new DefaultConverter();
        }
        switch (type) {
        case "number":
            return new CssNumberConverter(false);
        case "size":
            return new CssSizeConverter(false);
        case "word":
            return new CssWordConverter();
        case "paint":
            return new CssPaintableConverter(false);
        case "font":
            return new CssFontConverter(false);
        default:
            throw new IllegalArgumentException("illegal type:" + type);
        }
    }

}
