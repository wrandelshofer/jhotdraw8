/*
 * @(#)ResolvingConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.text;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

/**
 * Converters that implement this interface require non-null values
 * for {@link IdSupplier} and {@link IdResolver}.
 *
 * @param <T> the type of the values that can be converted
 */
public interface ResolvingConverter<T> extends Converter<T> {


}
