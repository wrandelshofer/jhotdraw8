/*
 * @(#)NulllableBooleanStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.text.CssBooleanConverter;
import org.jhotdraw8.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;

/**
 * Nullable BooleanStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NulllableBooleanStyleableKey extends SimpleStyleableKey<Boolean> implements WritableStyleableMapAccessor<Boolean> {

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
