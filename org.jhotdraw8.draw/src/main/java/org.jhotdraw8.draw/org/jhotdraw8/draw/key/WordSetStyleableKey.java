/*
 * @(#)WordSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.SetCssConverter;
import org.jhotdraw8.draw.css.converter.IdentCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;

/**
 * WordSetStyleableKey.
 *
 */
public class WordSetStyleableKey extends NonNullSetStyleableKey<String> {


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public WordSetStyleableKey(String name) {
        this(name, ChampVectorSet.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetStyleableKey(String name, PersistentSequencedSet<String> defaultValue) {
        this(name,
                new SetCssConverter<>(new IdentCssConverter(false)),
                defaultValue);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetStyleableKey(String name, CssConverter<PersistentSequencedSet<String>> converter, PersistentSequencedSet<String> defaultValue) {
        super(name,
                new SimpleParameterizedType(PersistentSequencedSet.class, String.class),
                converter,
                defaultValue);
    }
}
