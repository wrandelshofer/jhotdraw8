/*
 * @(#)CssInsetsStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssInsets;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.InsetsCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * InsetsStyleableMapAccessor.
 *
 */
public class CssInsetsStyleableMapAccessor
        extends AbstractStyleableMapAccessor<CssInsets>
        implements NonNullMapAccessor<CssInsets> {


    private final NonNullMapAccessor<CssSize> topKey;
    private final NonNullMapAccessor<CssSize> rightKey;
    private final NonNullMapAccessor<CssSize> bottomKey;
    private final NonNullMapAccessor<CssSize> leftKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param topKey    the insets top key
     * @param rightKey  the insets right key
     * @param bottomKey the insets bottom key
     * @param leftKey   the insets left key
     */
    public CssInsetsStyleableMapAccessor(String name, NonNullMapAccessor<CssSize> topKey, NonNullMapAccessor<CssSize> rightKey, NonNullMapAccessor<CssSize> bottomKey, NonNullMapAccessor<CssSize> leftKey) {
        super(name, CssInsets.class, new NonNullMapAccessor<?>[]{topKey, rightKey, bottomKey, leftKey}, new CssInsets(topKey.getDefaultValue(), rightKey.getDefaultValue(), bottomKey.getDefaultValue(), leftKey.getDefaultValue()));

        this.topKey = topKey;
        this.rightKey = rightKey;
        this.bottomKey = bottomKey;
        this.leftKey = leftKey;
    }

    private final Converter<CssInsets> converter = new InsetsCssConverter(false);

    @Override
    public Converter<CssInsets> getCssConverter() {
        return converter;
    }

    @Override
    public @Nullable CssInsets get(Map<? super Key<?>, Object> a) {
        final CssSize top = topKey.get(a);
        final CssSize right = rightKey.get(a);
        final CssSize bottom = bottomKey.get(a);
        final CssSize left = leftKey.get(a);
        if (top == null || right == null || bottom == null | left == null) {
            return null;
        }
        return new CssInsets(
                top,
                right,
                bottom,
                left
        );
    }


    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssInsets value) {
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
    public @Nullable CssInsets remove(Map<? super Key<?>, Object> a) {
        CssInsets oldValue = get(a);
        topKey.remove(a);
        rightKey.remove(a);
        bottomKey.remove(a);
        leftKey.remove(a);
        return oldValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable CssInsets value) {
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
