/*
 * @(#)NullableSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.SetCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;

import java.lang.reflect.Type;

/**
 * NonNullSetStyleableKey.
 *
 * @param <T> the element type of the set
 */
public class NullableSetStyleableKey<T> extends AbstractReadOnlyStyleableKey<PersistentSequencedSet<T>>
        implements WritableStyleableMapAccessor<PersistentSequencedSet<T>>,
        NullableKey<PersistentSequencedSet<T>> {


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public NullableSetStyleableKey(String name, Type elementType, CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(PersistentSequencedSet.class, elementType), new SetCssConverter<>(elementConverter), ChampVectorSet.of());
    }
}
