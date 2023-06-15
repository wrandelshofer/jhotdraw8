/*
 * @(#)Nullable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The Nullable annotation indicates that the {@code null} value is
 * allowed for the annotated element.
 */
@Documented
@Retention(CLASS)
@Target({TYPE_USE})
public @interface Nullable {
}
