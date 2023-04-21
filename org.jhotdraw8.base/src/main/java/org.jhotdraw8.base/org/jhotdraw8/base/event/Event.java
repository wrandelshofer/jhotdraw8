/*
 * @(#)Event.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.event;

import org.jhotdraw8.annotation.NonNull;

import java.util.EventObject;

/**
 * Event.
 *
 * @param <S> the type of the event source
 * @author Werner Randelshofer
 */
public class Event<S> extends EventObject {

    private static final long serialVersionUID = 1L;

    public Event(@NonNull S source) {
        super(source);
    }

    @Override
    public @NonNull S getSource() {
        @SuppressWarnings("unchecked")
        S temp = (S) super.getSource();
        return temp;
    }

}
