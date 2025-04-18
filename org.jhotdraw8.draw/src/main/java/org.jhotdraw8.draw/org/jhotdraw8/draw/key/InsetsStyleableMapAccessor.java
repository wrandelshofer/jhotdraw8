/*
 * @(#)InsetsStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Insets;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.InsetsConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * InsetsStyleableMapAccessor.
 *
 */
public class InsetsStyleableMapAccessor extends AbstractStyleableMapAccessor<Insets> {


    private final MapAccessor<Double> topKey;
    private final MapAccessor<Double> rightKey;
    private final MapAccessor<Double> bottomKey;
    private final MapAccessor<Double> leftKey;
    private final Converter<Insets> converter = new InsetsConverter(false);

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param topKey    the insets top key
     * @param rightKey  the insets right key
     * @param bottomKey the insets bottom key
     * @param leftKey   the insets left key
     */
    public InsetsStyleableMapAccessor(String name, MapAccessor<Double> topKey, MapAccessor<Double> rightKey, MapAccessor<Double> bottomKey, MapAccessor<Double> leftKey) {
        super(name, Insets.class, new MapAccessor<?>[]{topKey, rightKey, bottomKey, leftKey}, new Insets(topKey.getDefaultValue(), rightKey.getDefaultValue(), bottomKey.getDefaultValue(), leftKey.getDefaultValue()));

        this.topKey = topKey;
        this.rightKey = rightKey;
        this.bottomKey = bottomKey;
        this.leftKey = leftKey;
    }

    @Override
    public Insets get(Map<? super Key<?>, Object> a) {
        final Double top = topKey.get(a);
        final Double right = rightKey.get(a);
        final Double bottom = bottomKey.get(a);
        final Double left = leftKey.get(a);
        return new Insets(
                top == null ? 0.0 : top,
                right == null ? 0.0 : right,
                bottom == null ? 0.0 : bottom,
                left == null ? 0.0 : left
        );
    }

    @Override
    public Converter<Insets> getCssConverter() {
        return converter;
    }

    @Override
    public Insets remove(Map<? super Key<?>, Object> a) {
        Insets oldValue = get(a);
        topKey.remove(a);
        rightKey.remove(a);
        bottomKey.remove(a);
        leftKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable Insets value) {
        if (value == null) {
            remove(a);
        } else {
            topKey.put(a, value.getTop());
            rightKey.put(a, value.getRight());
            bottomKey.put(a, value.getBottom());
            leftKey.put(a, value.getLeft());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable Insets value) {
        if (value == null) {
            return remove(a);
        } else {
            a = topKey.put(a, value.getTop());
            a = rightKey.put(a, value.getRight());
            a = bottomKey.put(a, value.getBottom());
            return leftKey.put(a, value.getLeft());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        a = topKey.remove(a);
        a = rightKey.remove(a);
        a = bottomKey.remove(a);
        return leftKey.remove(a);
    }
}
