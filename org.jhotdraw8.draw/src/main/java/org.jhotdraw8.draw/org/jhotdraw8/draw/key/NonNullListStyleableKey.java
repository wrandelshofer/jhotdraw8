/*
 * @(#)NonNullListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * NonNullListStyleableKey.
 *
 * @param <T> the element type of the list
 */
public class NonNullListStyleableKey<T> extends AbstractReadOnlyStyleableKey<PersistentList<T>>
        implements WritableStyleableMapAccessor<PersistentList<T>>,
        NonNullKey<PersistentList<T>> {


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public NonNullListStyleableKey(String name, Type elementType, CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter), VectorList.of());
    }


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param elementType         the class of the type
     * @param elementConverter    String converter for a list element
     * @param defaultValue The default value.
     */
    public NonNullListStyleableKey(String name, Type elementType, CssConverter<T> elementConverter, PersistentList<T> defaultValue) {
        super(name, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter), defaultValue);
    }

    public NonNullListStyleableKey(String xmlName, String cssName, Type elementType, CssConverter<T> elementConverter, PersistentList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter), defaultValue);
    }

    public NonNullListStyleableKey(String xmlName, String cssName, Type elementType, CssConverter<T> elementConverter,
                                   @Nullable String delimiter, PersistentList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter, delimiter), defaultValue);
    }

    public NonNullListStyleableKey(String xmlName, String cssName, Type elementType, CssConverter<T> elementConverter,
                                   @Nullable String delimiter, @Nullable String prefix, @Nullable String suffix, PersistentList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(PersistentList.class, elementType), new ListCssConverter<>(elementConverter, delimiter, prefix, suffix), defaultValue);
    }

}
