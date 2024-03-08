/*
 * @(#)CompositeMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.util.Map;

/**
 * CompositeMapAccessor composes one or more {@link MapAccessor}s.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public interface CompositeMapAccessor<T> extends MapAccessor<T> {

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


    @NonNull ImmutableSequencedSet<MapAccessor<?>> getSubAccessors();

}
