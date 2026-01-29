/*
 * @(#)NonNullBooleanStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.BooleanCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadableStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * NonNullBooleanStyleableKey (not nullable).
 *
 */
public class NonNullBooleanStyleableKey extends NonNullObjectStyleableKey<Boolean>
        implements WritableStyleableMapAccessor<Boolean>,
        NonNullKey<Boolean> {


    public NonNullBooleanStyleableKey(String key) {
        this(key, ReadableStyleableMapAccessor.toCssName(key), false);
    }

    public NonNullBooleanStyleableKey(String key, Boolean defaultValue) {
        this(key, ReadableStyleableMapAccessor.toCssName(key), defaultValue);
    }

    public NonNullBooleanStyleableKey(String key, String cssName) {
        this(key, ReadableStyleableMapAccessor.toCssName(key), false);
    }

    public NonNullBooleanStyleableKey(String key, String cssName, Boolean defaultValue) {
        super(key, Boolean.class, new BooleanCssConverter(false), defaultValue);

    }

}