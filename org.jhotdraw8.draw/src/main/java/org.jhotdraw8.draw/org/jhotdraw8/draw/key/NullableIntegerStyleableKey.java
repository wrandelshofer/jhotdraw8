/*
 * @(#)NullableIntegerStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.IntegerCssConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableIntegerStyleableKey.
 *
 */
public class NullableIntegerStyleableKey extends NullableObjectStyleableKey<Integer> implements WritableStyleableMapAccessor<Integer> {


    public NullableIntegerStyleableKey(String name) {
        this(name, ReadOnlyStyleableMapAccessor.toCssName(name));
    }

    public NullableIntegerStyleableKey(String xmlName, String cssName) {
        super(xmlName, cssName, Integer.class, new IntegerCssConverter(true), null);
    }

    public NullableIntegerStyleableKey(String xmlName, String cssName, CssConverter<Integer> converter) {
        super(xmlName, cssName, Integer.class,converter,null);
    }


}
