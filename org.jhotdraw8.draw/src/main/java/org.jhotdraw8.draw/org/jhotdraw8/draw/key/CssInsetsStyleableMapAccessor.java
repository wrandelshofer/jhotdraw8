/*
 * @(#)CssInsetsStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.CssInsetsConverter;
import org.jhotdraw8.draw.css.value.CssInsets;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.io.Serial;
import java.util.Map;

/**
 * InsetsStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class CssInsetsStyleableMapAccessor
        extends AbstractStyleableMapAccessor<@NonNull CssInsets>
        implements NonNullMapAccessor<@NonNull CssInsets> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull NonNullMapAccessor<CssSize> topKey;
    private final @NonNull NonNullMapAccessor<CssSize> rightKey;
    private final @NonNull NonNullMapAccessor<CssSize> bottomKey;
    private final @NonNull NonNullMapAccessor<CssSize> leftKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param topKey    the insets top key
     * @param rightKey  the insets right key
     * @param bottomKey the insets bottom key
     * @param leftKey   the insets left key
     */
    public CssInsetsStyleableMapAccessor(@NonNull String name, @NonNull NonNullMapAccessor<CssSize> topKey, @NonNull NonNullMapAccessor<CssSize> rightKey, @NonNull NonNullMapAccessor<CssSize> bottomKey, @NonNull NonNullMapAccessor<CssSize> leftKey) {
        super(name, CssInsets.class, new NonNullMapAccessor<?>[]{topKey, rightKey, bottomKey, leftKey}, new CssInsets(topKey.getDefaultValue(), rightKey.getDefaultValue(), bottomKey.getDefaultValue(), leftKey.getDefaultValue()));

        this.topKey = topKey;
        this.rightKey = rightKey;
        this.bottomKey = bottomKey;
        this.leftKey = leftKey;
    }

    private final Converter<CssInsets> converter = new CssInsetsConverter(false);

    @Override
    public @NonNull Converter<CssInsets> getCssConverter() {
        return converter;
    }

    @Override
    public @Nullable CssInsets get(@NonNull Map<? super Key<?>, Object> a) {
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
    public void set(@NonNull Map<? super Key<?>, Object> a, @Nullable CssInsets value) {
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
    public @Nullable CssInsets remove(@NonNull Map<? super Key<?>, Object> a) {
        CssInsets oldValue = get(a);
        topKey.remove(a);
        rightKey.remove(a);
        bottomKey.remove(a);
        leftKey.remove(a);
        return oldValue;
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> put(@NonNull ImmutableMap<Key<?>, Object> a, @Nullable CssInsets value) {
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
    public @NonNull ImmutableMap<Key<?>, Object> remove(@NonNull ImmutableMap<Key<?>, Object> a) {
        a = topKey.remove(a);
        a = rightKey.remove(a);
        a = bottomKey.remove(a);
        return leftKey.remove(a);
    }
}
