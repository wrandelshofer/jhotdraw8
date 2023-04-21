/*
 * @(#)WordSetKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullKey;

/**
 * WordSetKey.
 *
 * @author Werner Randelshofer
 */
public class WordSetKey extends SimpleNonNullKey<@NonNull ImmutableSet<String>> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public WordSetKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetKey(@NonNull String name, @NonNull ImmutableSet<String> defaultValue) {
        super(name, new TypeToken<ImmutableSet<String>>() {
        }, defaultValue);
    }
}
