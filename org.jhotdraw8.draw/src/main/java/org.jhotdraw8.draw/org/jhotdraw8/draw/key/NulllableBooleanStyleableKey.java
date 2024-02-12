/*
 * @(#)NulllableBooleanStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssBooleanConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.io.Serial;

/**
 * Nullable BooleanStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NulllableBooleanStyleableKey extends NullableObjectStyleableKey<Boolean> implements WritableStyleableMapAccessor<Boolean> {

    @Serial
    private static final long serialVersionUID = 1L;

    public NulllableBooleanStyleableKey(String key) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), null);
    }

    public NulllableBooleanStyleableKey(String key, Boolean defaultValue) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), defaultValue);
    }

    public NulllableBooleanStyleableKey(String key, String cssName) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), null);
    }

    public NulllableBooleanStyleableKey(String key, @NonNull String cssName, Boolean defaultValue) {
        super(key, Boolean.class, new CssBooleanConverter(true), defaultValue);
    }

}
