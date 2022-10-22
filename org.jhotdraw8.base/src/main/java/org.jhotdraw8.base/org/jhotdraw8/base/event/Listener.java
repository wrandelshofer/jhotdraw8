/*
 * @(#)Listener.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.event;

/**
 * Functional listener interface.
 *
 * @author Werner Randelshofer
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
