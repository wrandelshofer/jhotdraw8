/*
 * @(#)SvgDefaultablePaintStyleableMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.key;

import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;

public interface SvgDefaultablePaintStyleableMapAccessor<T extends Paintable> extends NonNullMapAccessor<SvgDefaultablePaint<T>> {
    T getInitialValue();
}
