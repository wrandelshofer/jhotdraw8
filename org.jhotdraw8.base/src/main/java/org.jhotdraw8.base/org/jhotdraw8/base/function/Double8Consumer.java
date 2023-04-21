/*
 * @(#)Double8Consumer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.base.function;

/**
 * Double6Consumer.
 *
 * @author Werner Randelshofer
 */
@FunctionalInterface
public interface Double8Consumer {
    /**
     * Performs this operation on the given argument.
     *
     * @param v1 the input argument
     * @param v2 the input argument
     * @param v3 the input argument
     * @param v4 the input argument
     * @param v5 the input argument
     * @param v6 the input argument
     * @param v7 the input argument
     * @param v8 the input argument
     */
    void accept(double v1, double v2, double v3, double v4, double v5, double v6, double v7, double v8);

}
