/*
 * @(#)ResolvingConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.base.converter;

/**
 * Converters that implement this interface require non-null values
 * for {@link IdSupplier} and {@link IdResolver}.
 *
 * @param <T> the type of the values that can be converted
 */
public interface ResolvingConverter<T> extends Converter<T> {
    @Override
    default boolean needsIdResolver() {
        return true;
    }
}
