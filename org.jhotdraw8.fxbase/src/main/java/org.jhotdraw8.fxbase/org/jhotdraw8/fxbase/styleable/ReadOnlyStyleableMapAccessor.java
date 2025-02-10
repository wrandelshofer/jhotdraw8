/*
 * @(#)ReadOnlyStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Generic interface for map accessors that are readable by CSS.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for APIs that provide read methods but no write methods.
 *
 * @param <T> The value type.
 */
public interface ReadOnlyStyleableMapAccessor<T> extends MapAccessor<T> {

    long serialVersionUID = 1L;

    /**
     * Gets the converter.
     *
     * @return the converter
     */
    Converter<T> getCssConverter();

    /**
     * Returns the CSS name string.
     * <p>
     * The default implementation converts the name from "camel case" to "dash
     * separated words".
     *
     * @return name string.
     */
    String getCssName();

    /**
     * Returns the CSS namespace uri.
     * <p>
     * The default implementation returns null.
     *
     * @return namespace uri string.
     */
    default @Nullable String getCssNamespace() {
        return null;
    }

    /**
     * Returns the CSS name string.
     * <p>
     * Converts the name from "camelCase" to "kebab-case".
     *
     * @param camelCaseName string
     * @return cssName string.
     */
    static String toCssName(String camelCaseName) {
        final StringBuilder b = new StringBuilder();
        final String name = camelCaseName;
        boolean insertDash = false;
        for (int i = 0, n = name.length(); i < n; i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (insertDash) {
                    b.append('-');
                }
                b.append(Character.toLowerCase(ch));
                insertDash = false;
            } else {
                b.append(ch);
                insertDash = true;
            }
        }
        return b.toString();
    }

}
