/*
 * @(#)NonNullSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;

import java.lang.reflect.Type;

/**
 * NonNullSetStyleableKey.
 *
 * @param <T> the element type of the set
 * @author Werner Randelshofer
 */
public class NonNullSetStyleableKey<T> extends AbstractReadOnlyStyleableKey<PersistentSequencedSet<T>>
        implements WritableStyleableMapAccessor<PersistentSequencedSet<T>>,
        NonNullKey<PersistentSequencedSet<T>> {


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param type      the class of the type
     * @param converter String converter for a list element
     */
    public NonNullSetStyleableKey(String name, Type type, CssConverter<PersistentSequencedSet<T>> converter) {
        super(name, type, converter, ChampVectorSet.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param type         the class of the type
     * @param converter    String converter for a list element
     * @param defaultValue The default value.
     */
    public NonNullSetStyleableKey(String name, Type type, CssConverter<PersistentSequencedSet<T>> converter, PersistentSequencedSet<T> defaultValue) {
        super(name, type, converter, defaultValue);
    }
/*
    public NonNullSetStyleableKey(String xmlName, String cssName, TypeToken<PersistentSequencedSet<T>> type, CssConverter<T> converter, PersistentSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter), defaultValue);
    }
    public NonNullSetStyleableKey(String xmlName, String cssName, TypeToken<PersistentSequencedSet<T>> type, CssConverter<T> converter,
                           @Nullable String delimiter, PersistentSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter,delimiter), defaultValue);
    }
*/
}
