/*
 * @(#)WritableStyleableMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.styleable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.text.CssConverter;
import org.jhotdraw8.text.Converter;

/**
 * Interface for keys which support styled values from CSS.
 *
 * @param <T> The value type.
 * @author Werner Randelshofer
 */
public interface WritableStyleableMapAccessor<T> extends ReadOnlyStyleableMapAccessor<T> {

    long serialVersionUID = 1L;

    /**
     * Gets examples.
     *
     * @return a help text.
     */
    default @NonNull ImmutableList<String> getExamples() {
        Converter<T> cssConverter = getCssConverter();
        if (cssConverter instanceof CssConverter<?>) {
            return ((CssConverter<?>) cssConverter).getExamples();
        }
        return ImmutableArrayList.of();
    }


}
