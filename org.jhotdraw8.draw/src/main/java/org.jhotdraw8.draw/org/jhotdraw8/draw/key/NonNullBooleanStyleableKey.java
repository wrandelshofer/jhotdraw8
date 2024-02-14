/*
 * @(#)NonNullBooleanStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.BooleanCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

import java.io.Serial;

/**
 * NonNullBooleanStyleableKey (not nullable).
 *
 * @author Werner Randelshofer
 */
public class NonNullBooleanStyleableKey extends NonNullObjectStyleableKey<Boolean>
        implements WritableStyleableMapAccessor<Boolean>,
        NonNullKey<Boolean> {

    @Serial
    private static final long serialVersionUID = 1L;

    public NonNullBooleanStyleableKey(@NonNull String key) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), false);
    }

    public NonNullBooleanStyleableKey(@NonNull String key, @NonNull Boolean defaultValue) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), defaultValue);
    }

    public NonNullBooleanStyleableKey(@NonNull String key, @NonNull String cssName) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), false);
    }

    public NonNullBooleanStyleableKey(@NonNull String key, @NonNull String cssName, @NonNull Boolean defaultValue) {
        super(key, Boolean.class, new BooleanCssConverter(false), defaultValue);

    }

}