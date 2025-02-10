/*
 * @(#)NulllableBooleanStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.BooleanCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * Nullable NonNullBooleanStyleableKey.
 *
 */
public class NulllableBooleanStyleableKey extends NullableObjectStyleableKey<Boolean> implements WritableStyleableMapAccessor<Boolean> {


    public NulllableBooleanStyleableKey(String key) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), null);
    }

    public NulllableBooleanStyleableKey(String key, Boolean defaultValue) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), defaultValue);
    }

    public NulllableBooleanStyleableKey(String key, String cssName) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), null);
    }

    public NulllableBooleanStyleableKey(String key, String cssName, Boolean defaultValue) {
        super(key, Boolean.class, new BooleanCssConverter(true), defaultValue);
    }

}
