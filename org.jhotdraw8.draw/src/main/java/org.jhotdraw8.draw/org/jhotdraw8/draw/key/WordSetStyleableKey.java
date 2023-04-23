/*
 * @(#)WordSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.SequencedChampSet;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssSetConverter;
import org.jhotdraw8.draw.css.converter.CssIdentConverter;

import java.io.Serial;

/**
 * WordSetStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class WordSetStyleableKey extends SetStyleableKey<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public WordSetStyleableKey(@NonNull String name) {
        this(name, SequencedChampSet.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetStyleableKey(@NonNull String name, @NonNull ImmutableSequencedSet<String> defaultValue) {
        this(name,
                new CssSetConverter<String>(new CssIdentConverter(false)),
                defaultValue);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetStyleableKey(@NonNull String name, @NonNull CssConverter<ImmutableSequencedSet<String>> converter, @NonNull ImmutableSequencedSet<String> defaultValue) {
        super(name,
                new TypeToken<ImmutableSequencedSet<String>>() {
                },
                converter,
                defaultValue);
    }
}
