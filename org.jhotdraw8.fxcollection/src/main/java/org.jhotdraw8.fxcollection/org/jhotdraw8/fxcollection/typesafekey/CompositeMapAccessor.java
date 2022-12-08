/*
 * @(#)CompositeMapAccessor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
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

    // FIXME refactor this to ReadOnlyCollection, because we do not allow writes
    @NonNull
    Collection<MapAccessor<?>> getSubAccessors();

}
