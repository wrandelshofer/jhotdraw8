/*
 * @(#)ReadOnlyKeyMapEntryProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import javafx.collections.ObservableMap;
import org.jhotdraw8.fxcollection.MapEntryProperty;
import org.jspecify.annotations.Nullable;

/**
 * KeyMapEntryProperty.
 * <p>
 * FIXME currently only works fully if the provided MapAccessor is an instance
 * of Key.
 *
 * @param <V> the value type
 */
public class ReadOnlyKeyMapEntryProperty<V> extends MapEntryProperty<Key<?>, Object, V> {
    private final MapAccessor<V> accessor;

    public ReadOnlyKeyMapEntryProperty(ObservableMap<Key<?>, Object> map, MapAccessor<V> key) {
        super(map, (key instanceof Key<?>) ? (Key<?>) key : null, key.getRawValueType());
        this.accessor = key;
    }

    @Override
    public @Nullable V get() {
        return accessor.get(map);
    }

    @Override
    public void set(V value) {
        accessor.put(map, value);

        // Note: super must be called after "put", so that listeners
        //       can be properly informed.
        super.set(value);
    }

    @Override
    public String getName() {
        return accessor.toString();
    }
}
