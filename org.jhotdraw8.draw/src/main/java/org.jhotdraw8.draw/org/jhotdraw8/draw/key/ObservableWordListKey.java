/*
 * @(#)ObservableWordListKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullKey;

/**
 * ObservableWordListKey.
 *
 * @author Werner Randelshofer
 */
public class ObservableWordListKey extends SimpleNonNullKey<@NonNull ImmutableList<String>> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public ObservableWordListKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public ObservableWordListKey(@NonNull String name, @NonNull ImmutableList<String> defaultValue) {
        super(name, new TypeToken<ImmutableList<String>>() {
        }, defaultValue);
    }
}
