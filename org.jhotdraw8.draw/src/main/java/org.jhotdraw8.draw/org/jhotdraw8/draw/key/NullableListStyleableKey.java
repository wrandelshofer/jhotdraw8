/*
 * @(#)NullableListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.lang.reflect.Type;

/**
 * Nullable NonNullListStyleableKey.
 *
 * @param <T> the element type of the list
 */
public class NullableListStyleableKey<T> extends AbstractReadableStyleableKey<PersistentList<T>>
        implements WritableStyleableMapAccessor<PersistentList<T>>, NullableKey<PersistentList<T>> {


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name             The name of the key.
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public NullableListStyleableKey(String name, Type elementType, CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter), VectorList.of());
    }
}
