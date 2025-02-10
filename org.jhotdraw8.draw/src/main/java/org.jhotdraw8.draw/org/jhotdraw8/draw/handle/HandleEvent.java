/*
 * @(#)HandleEvent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import org.jhotdraw8.base.event.Event;

import java.io.Serial;

/**
 * HandleEvent.
 *
 */
public class HandleEvent extends Event<Handle> {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum EventType {

        FIGURE_ADDED,
        FIGURE_REMOVED,
        PROPERTY_CHANGED,
        FIGURE_INVALIDATED
    }

    private final EventType eventType;

    public <T> HandleEvent(Handle source, EventType type) {
        super(source);
        this.eventType = type;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "HandleEvent{" + "type=" + eventType + " handle=" + getSource()
                + '}';
    }

}
