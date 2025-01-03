/*
 * @(#)SourceLocator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.ast;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Indicates the position of a node in a source file, intended primarily for error reporting.
 *
 * @param characterOffset the character offset or -1 if unknown
 * @param lineNumber      the line number or -1 if unknown. A file starts with line number 1 (not with 0).
 * @param uri             the source file URI or null if unknown
 */
public record SourceLocator(int characterOffset, int lineNumber,
                            @Nullable URI uri) {

}
