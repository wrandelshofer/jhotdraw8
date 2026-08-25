/*
 * @(#)RunnableWithException.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.concurrent;

/// A runnable that can throw a checked exception.
@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
