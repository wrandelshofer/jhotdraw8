/*
 * @(#)MapEntryProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;

import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * This property is weakly bound to an entry in a map.
 *
 * @param <K> key type
 * @param <V> map value type
 * @param <T> entry value type
 * @author Werner Randelshofer
 */
public class MapEntryProperty<K, V, T extends V> extends ObjectPropertyBase<T>
        implements MapChangeListener<K, V> {

    protected @NonNull K key;
    protected @NonNull ObservableMap<K, V> map;
    private @Nullable WeakMapChangeListener<K, V> weakListener;
    /**
     * Here char is used as an uint16.
     */
    private char changing;

    public MapEntryProperty(@NonNull ObservableMap<K, V> map, @NonNull K key, @NonNull Type tClazz) {
        this.map = map;
        this.key = key;

        map.addListener(weakListener = new WeakMapChangeListener<>(this));
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
    public void onChanged(@NonNull Change<? extends K, ? extends V> change) {
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
        V temp = value;
        map.put(key, temp);

        // Note: super must be called after "put", so that listeners
        //       can be properly informed.
        super.set(value);
    }

    @Override
    public void unbind() {
        super.unbind();
        if (map != null) {
            map.removeListener(weakListener);
            weakListener = null;
            map = null;
            key = null;
        }
    }
}
