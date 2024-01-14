/*
 * @(#)DoubleConsumer2.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

/**
 * DoubleConsumer2.
 *
 * @author Werner Randelshofer
 */
@FunctionalInterface
public interface DoubleConsumer2 {

    /**
     * Performs this operation on the given argument.
     *
     * @param v1 the input argument
     * @param v2 the input argument
     */
    void accept(double v1, double v2);

}
