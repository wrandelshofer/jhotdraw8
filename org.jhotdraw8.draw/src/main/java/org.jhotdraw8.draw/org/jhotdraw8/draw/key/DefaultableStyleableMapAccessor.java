/*
 * @(#)DefaultableStyleableMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;


public interface DefaultableStyleableMapAccessor<T> extends NonNullMapAccessor<CssDefaultableValue<T>> {
    T getInitialValue();
}
