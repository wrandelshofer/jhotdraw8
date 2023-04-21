/*
 * @(#)WritableStyleableMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.vector.VectorList;

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
        /*
        if (cssConverter instanceof CssConverter<?>) {
            return ((CssConverter<?>) cssConverter).getExamples();
        }*/
        return VectorList.of();
    }


}
