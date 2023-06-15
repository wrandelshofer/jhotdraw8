/*
 * @(#)NonNull.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The NonNull annotation indicates that the {@code null} value is
 * forbidden for the annotated element.
 */
@Documented
@Retention(CLASS)
@Target({TYPE_USE})
public @interface NonNull {
}
