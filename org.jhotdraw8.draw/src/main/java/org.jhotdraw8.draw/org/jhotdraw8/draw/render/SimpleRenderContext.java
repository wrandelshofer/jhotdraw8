/*
 * @(#)SimpleRenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleRenderContext implements WritableRenderContext {
    private final Map<Figure, Node> nodeMap = new HashMap<>();
    private final ObservableMap<Key<?>, Object> properties = FXCollections.observableHashMap();

    public SimpleRenderContext() {
    }

    @Override
    public Node getNode(Figure figure) {
        return nodeMap.computeIfAbsent(figure, f -> f.createNode(this));
    }

    public ObservableMap<Key<?>, Object> getProperties() {
        return properties;
    }

    @Override
    public <T> void set(MapAccessor<T> key, @Nullable T value) {
        key.set(properties, value);
    }

    /**
     * Gets a property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    @Override
    public @Nullable <T> T get(MapAccessor<T> key) {
        return key.get(getProperties());
    }

    /**
     * Gets a nonnull property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    @Override
    public <T> T getNonNull(NonNullMapAccessor<T> key) {
        T value = key.get(getProperties());
        return Objects.requireNonNull(value, "value");
    }

}
