/*
 * @(#)RunnableWithException.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
