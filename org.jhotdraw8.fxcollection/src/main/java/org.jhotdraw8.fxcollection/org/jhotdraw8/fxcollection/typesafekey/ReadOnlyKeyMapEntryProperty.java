/*
 * @(#)KeyMapEntryProperty.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import javafx.collections.ObservableMap;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxcollection.MapEntryProperty;

/**
 * KeyMapEntryProperty.
 * <p>
 * FIXME currently only works fully if the provided MapAccessor is an instance
 * of Key.
 *
 * @author Werner Randelshofer
 */
public class ReadOnlyKeyMapEntryProperty<V> extends MapEntryProperty<Key<?>, Object, V> {
    private final @NonNull MapAccessor<V> accessor;

    public ReadOnlyKeyMapEntryProperty(@NonNull ObservableMap<Key<?>, Object> map, MapAccessor<V> key) {
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
    public @Nullable Object getBean() {
        return map;
    }

    @Override
    public String getName() {
        return accessor.toString();
    }
}
