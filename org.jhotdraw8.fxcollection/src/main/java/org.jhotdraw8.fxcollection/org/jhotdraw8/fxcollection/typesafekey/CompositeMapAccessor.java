/*
 * @(#)CompositeMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;

import java.io.Serial;
import java.util.Map;

/**
 * CompositeMapAccessor composes one or more {@link MapAccessor}s.
 *
 * @param <T> the value type
 */
public interface CompositeMapAccessor<T> extends MapAccessor<T> {

    /**
     * Serial version UID:
     */
    @Serial
    long serialVersionUID = 1L;

    @Override
    default boolean containsKey(Map<Key<?>, Object> map) {
        for (MapAccessor<?> sub : getSubAccessors()) {
            if (!sub.containsKey(map)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all {@link MapAccessor}s that this accessor is composing.
     * @return the sub-accessors
     */
    PersistentSequencedSet<MapAccessor<?>> getSubAccessors();

}
