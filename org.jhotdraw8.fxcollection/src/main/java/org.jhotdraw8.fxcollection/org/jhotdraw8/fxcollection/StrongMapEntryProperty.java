/*
 * @(#)StrongMapEntryProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;

import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * This property is strongly bound to an entry in a map.
 *
 * @param <K> key type
 * @param <V> map value type
 * @param <T> entry value type
 */
public class StrongMapEntryProperty<K, V, T extends V> extends ObjectPropertyBase<T>
        implements MapChangeListener<K, V> {

    private @Nullable K key;
    private @Nullable ObservableMap<K, V> map;
    /**
     * Here char is used as an uint16.
     */
    private char changing;

    public StrongMapEntryProperty(ObservableMap<K, V> map, K key, Class<T> tClazz) {
        this.map = map;
        this.key = key;

        map.addListener(this);
    }

    @Override
    public @Nullable T get() {
        @SuppressWarnings("unchecked")
        T temp = (T) map.get(key);
        return temp;
    }

    @Override
    public @Nullable Object getBean() {
        return map;
    }

    @Override
    public String getName() {
        return key.toString();
    }

    @Override
    public void onChanged(Change<? extends K, ? extends V> change) {
        if (changing++ == 0) {
            if (this.key.equals(change.getKey())) {
                if (change.wasAdded()) {// was added, or removed and then added
                    @SuppressWarnings("unchecked")
                    T valueAdded = (T) change.getValueAdded();
                    if (!Objects.equals(super.get(), valueAdded)) {
                        set(valueAdded);
                    }
                } else if (change.wasRemoved()) {// was removed but not added
                    if (super.get() != null) {
                        set(null);
                    }
                }
            }
        }
        changing--;
    }

    @Override
    public void set(@Nullable T value) {
        // We must ignore calls to this method after unbind() has been called.
        if (map != null) {
            map.put(key, value);

            // Note: super must be called after "put", so that listeners
            //       can be properly informed.
            super.set(value);
        }
    }

    @Override
    public void unbind() {
        super.unbind();
        if (map != null) {
            map.removeListener(this);
            map = null;
            key = null;
        }
    }
}
