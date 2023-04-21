/*
 * @(#)DefaultableStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

/**
 * DefaultableStyleableMapAccessor.
 *
 * @param <T> the value type
 */
public interface DefaultableStyleableMapAccessor<T> extends NonNullMapAccessor<CssDefaultableValue<T>> {
    T getInitialValue();
}
