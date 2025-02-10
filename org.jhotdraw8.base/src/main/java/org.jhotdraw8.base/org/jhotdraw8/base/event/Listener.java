/*
 * @(#)Listener.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.event;

/**
 * Functional listener interface.
 *
 * @param <E> the event type
 */
@FunctionalInterface
public interface Listener<E> {

    /**
     * Handles an event.
     *
     * @param event the event
     */
    void handle(E event);
}
