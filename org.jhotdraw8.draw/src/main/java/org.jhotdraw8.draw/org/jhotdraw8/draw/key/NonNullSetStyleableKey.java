/*
 * @(#)NonNullSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.icollection.PersistentHashSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.lang.reflect.Type;

/// NonNullSetStyleableKey.
///
/// @param <T> the element type of the set
public class NonNullSetStyleableKey<T> extends AbstractReadableStyleableKey<PersistentSet<T>>
        implements WritableStyleableMapAccessor<PersistentSet<T>>,
        NonNullKey<PersistentSet<T>> {


    /// Creates a new instance with the specified name and with an empty list as the
    /// default value.
    ///
    /// @param name      The name of the key.
    /// @param type      the class of the type
    /// @param converter String converter for a list element
    public NonNullSetStyleableKey(String name, Type type, CssConverter<PersistentSet<T>> converter) {
        super(name, type, converter, PersistentHashSet.of());
    }

    /// Creates a new instance with the specified name, mask and default value.
    ///
    /// @param name         The name of the key.
    /// @param type         the class of the type
    /// @param converter    String converter for a list element
    /// @param defaultValue The default value.
    public NonNullSetStyleableKey(String name, Type type, CssConverter<PersistentSet<T>> converter, PersistentSet<T> defaultValue) {
        super(name, type, converter, defaultValue);
    }
/*
    public NonNullSetStyleableKey(String xmlName, String cssName, TypeToken<PersistentSet<T>> type, CssConverter<T> converter, PersistentSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter), defaultValue);
    }
    public NonNullSetStyleableKey(String xmlName, String cssName, TypeToken<PersistentSet<T>> type, CssConverter<T> converter,
                           @Nullable String delimiter, PersistentSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter,delimiter), defaultValue);
    }
*/
}
