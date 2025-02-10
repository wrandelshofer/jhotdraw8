/*
 * @(#)StyleableMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import javafx.collections.ObservableMap;
import javafx.css.StyleOrigin;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * A map which stores its values in an array, and which can share its keys with
 * other maps.
 * <p>
 * This map can store multiple values for each key.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface StyleableMap<K, V> extends ObservableMap<K, V> {
    <T extends K> boolean containsKey(StyleOrigin origin, T key);

    @Nullable
    V get(StyleOrigin origin, K key);

    Map<K, V> getMap(StyleOrigin origin);

    @Nullable
    StyleOrigin getStyleOrigin(K key);

    /**
     * Removes the specified key from the specified style origin
     * and puts the provided defaulting method for the key in place.
     *
     * @param origin the style origin
     * @param key    the key
     */
    V removeKey(StyleOrigin origin, K key);

    Map<K, V> getStyledMap();

    @Nullable
    V put(StyleOrigin styleOrigin, K key, @Nullable V value);

    void removeAll(StyleOrigin origin);

    void resetStyledValues();

    Set<Entry<K, V>> entrySet(StyleOrigin origin);
}
