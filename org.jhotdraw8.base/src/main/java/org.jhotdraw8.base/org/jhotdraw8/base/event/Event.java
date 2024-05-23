/*
 * @(#)Event.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.event;


import java.io.Serial;
import java.util.EventObject;

/**
 * Event.
 *
 * @param <S> the type of the event source
 * @author Werner Randelshofer
 */
public class Event<S> extends EventObject {

    @Serial
    private static final long serialVersionUID = 1L;

    public Event(S source) {
        super(source);
    }

    @Override
    public S getSource() {
        @SuppressWarnings("unchecked")
        S temp = (S) super.getSource();
        return temp;
    }

}
