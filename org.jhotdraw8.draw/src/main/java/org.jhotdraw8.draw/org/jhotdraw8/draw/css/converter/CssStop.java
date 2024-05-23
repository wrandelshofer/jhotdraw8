/*
 * @(#)CssStop.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.draw.css.value.CssColor;
import org.jspecify.annotations.Nullable;

/**
 * CssStop.
 *
 * @author Werner Randelshofer
 */
public record CssStop(@Nullable Double offset, CssColor color) {
}
