/*
 * @(#)NullableIntegerStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssIntegerConverter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * IntegerStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableIntegerStyleableKey extends SimpleStyleableKey<Integer> implements WritableStyleableMapAccessor<Integer> {

    private static final long serialVersionUID = 1L;

    public NullableIntegerStyleableKey(@NonNull String name) {
        this(name, ReadOnlyStyleableMapAccessor.toCssName(name));
    }

    public NullableIntegerStyleableKey(@NonNull String xmlName,@NonNull  String cssName) {
        super(xmlName, cssName, Integer.class,new CssIntegerConverter(true),null);
    }
    public NullableIntegerStyleableKey(@NonNull String xmlName,@NonNull  String cssName,@NonNull  CssConverter<Integer> converter) {
        super(xmlName, cssName, Integer.class,converter,null);
    }


}
