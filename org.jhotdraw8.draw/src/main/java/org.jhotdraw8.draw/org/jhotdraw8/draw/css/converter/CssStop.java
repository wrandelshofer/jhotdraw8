/*
 * @(#)CssStop.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.css.value.CssColor;

/**
 * CssStop.
 *
 * @author Werner Randelshofer
 */
public record CssStop(@Nullable Double offset, @NonNull CssColor color) {
}
