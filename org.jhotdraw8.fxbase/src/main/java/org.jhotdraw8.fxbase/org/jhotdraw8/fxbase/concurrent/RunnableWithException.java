package org.jhotdraw8.fxbase.concurrent;

@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
