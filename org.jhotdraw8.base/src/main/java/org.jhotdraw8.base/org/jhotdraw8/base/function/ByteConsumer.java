/*
 * @(#)ByteConsumer.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

/**
 * ByteConsumer.
 *
 * @author Werner Randelshofer
 */
@FunctionalInterface
public interface ByteConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param v the input argument
     */
    void accept(byte v);

}