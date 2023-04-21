/*
 * @(#)ReadOnlyMapEntryProperty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;

import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.lang.reflect.Type;

/**
 * This property is weakly bound to an entry in a map.
 *
 * @param <K> key type
 * @param <V> map value type
 * @param <T> entry value type
 * @author Werner Randelshofer
 */
public class ReadOnlyMapEntryProperty<K, V, T extends V> extends ReadOnlyObjectPropertyBase<T>
        implements MapChangeListener<K, V> {

    protected @NonNull K key;
    protected @NonNull ObservableMap<K, V> map;
    private @Nullable WeakMapChangeListener<K, V> weakListener;
    /**
     * Here char is used as an uint16.
     */
    private char changing;

    public ReadOnlyMapEntryProperty(@NonNull ObservableMap<K, V> map, @NonNull K key, @NonNull Type tClazz) {
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
                fireValueChangedEvent();
            }
        }
        changing--;
    }
}
