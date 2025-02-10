/*
 * @(#)AbstractStyleablePropertyBean.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.css.StyleOrigin;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AbstractStyleablePropertyBean.
 *
 */
public abstract class AbstractStyleablePropertyBean
        implements StyleablePropertyBean {
    protected static final Map<Class<?>, Map<Key<?>, Integer>> keyMaps = new ConcurrentHashMap<>();

    @SuppressWarnings("this-escape")
    protected final StyleableMap<Key<?>, Object> properties =
            createStyleableMap();

    public AbstractStyleablePropertyBean() {
    }

    private SimpleStyleableMap<Key<?>, Object> createStyleableMap() {
        // Explicit type arguments needed for Java 8!
        return new SimpleStyleableMap<>(createKeyMap()) {
            @Override
            @SuppressWarnings("unchecked")
            protected void callObservers(StyleOrigin origin, MapChangeListener.Change<Key<?>, Object> change) {
                final Key<Object> key = (Key<Object>) change.getKey();
                Object oldValue = change.wasRemoved() ? change.getValueRemoved() : key.getDefaultValue();
                Object newValue = change.wasAdded() ? change.getValueAdded() : key.getDefaultValue();
                if (!Objects.equals(oldValue, newValue)) {
                    onPropertyChanged(key,
                            oldValue,
                            newValue,
                            change.wasAdded(), change.wasRemoved());
                    AbstractStyleablePropertyBean.this.callObservers(origin, false, change);
                    super.callObservers(origin, change);
                }
            }
        };
    }

    /**
     * Creates a key map for the {@link SimpleStyleableMap} that
     * is used to store the properties of this object.
     * <p>
     * This implementation creates one key map for this class, and shares
     * it with all instances of this class.
     *
     * @return a new map
     */
    protected Map<Key<?>, Integer> createKeyMap() {
        return keyMaps.computeIfAbsent(getClass(), k -> {
            IdentityHashMap<Key<?>, Integer> m = new IdentityHashMap<>() {
                @Serial
                private static final long serialVersionUID = 0L;
                final AtomicInteger nextIndex = new AtomicInteger();

                @Override
                public Integer get(Object key) {
                    Integer v;
                    if ((v = super.get(key)) == null) {
                        Integer newValue;
                        newValue = nextIndex.getAndIncrement();
                        put((Key<?>) key, newValue);
                        return newValue;
                    }
                    return v;
                }
            };

            return m;
        });
    }

    /**
     * Returns the user properties.
     */
    @Override
    public final ObservableMap<Key<?>, Object> getProperties() {
        return properties;
    }

    protected StyleableMap<Key<?>, Object> getStyleableMap() {
        return properties;
    }

    /**
     * Returns the style value.
     */
    @Override
    public @Nullable <T> T getStyled(MapAccessor<T> key) {
        StyleableMap<Key<?>, Object> map = getStyleableMap();
        @SuppressWarnings("unchecked")
        T ret = key.get(map.getStyledMap());// key may invoke get multiple times!
        return ret;
    }

    @Override
    public <T> T getStyled(@Nullable StyleOrigin origin, MapAccessor<T> key) {
        if (origin == null) {
            return getStyled(key);
        }
        Map<Key<?>, Object> map = getStyleableMap().getMap(origin);
        return key.get(map);
    }

    @Override
    public <T> boolean containsMapAccessor(StyleOrigin origin, MapAccessor<T> key) {
        return key.containsKey(getStyleableMap().getMap(origin));
    }

    /**
     * Sets the style value.
     */
    @Override
    public @Nullable <T> T setStyled(StyleOrigin origin, MapAccessor<T> key, T newValue) {
        StyleableMap<Key<?>, Object> map = getStyleableMap();
        @SuppressWarnings("unchecked")
        T ret = key.put(map.getMap(origin), newValue);
        return ret;
    }

    @Override
    public @Nullable <T> T remove(StyleOrigin origin, MapAccessor<T> key) {
        @SuppressWarnings("unchecked")
        T ret = key.remove(getStyleableMap().getMap(origin));
        return ret;
    }

    @Override
    public void removeAll(StyleOrigin origin) {
        getStyleableMap().removeAll(origin);
    }

    /**
     * This method is invoked just before listeners are notified. This
     * implementation is empty.
     *
     * @param <T>        the type
     * @param key        the changed key
     * @param oldValue   the old value
     * @param newValue   the new value
     * @param wasAdded
     * @param wasRemoved
     */
    protected <T> void onPropertyChanged(Key<T> key, T oldValue, T newValue, boolean wasAdded, boolean wasRemoved) {
    }

    /**
     * This method is invoked just before listeners are notified. This
     * implementation is empty.
     *
     * @param origin     the style origin
     * @param willChange true if the change is about to be performed, false if
     *                   the change happened
     * @param change     the change
     */
    protected void callObservers(StyleOrigin origin, boolean willChange, MapChangeListener.Change<Key<?>, Object> change) {

    }

    @Override
    public void resetStyledValues() {
        properties.resetStyledValues();
    }
}
