/*
 * @(#)CssStop.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import org.jspecify.annotations.Nullable;

/**
 * CssStop.
 */
public record CssStop(@Nullable CssSize offset, CssColor color) {
}
