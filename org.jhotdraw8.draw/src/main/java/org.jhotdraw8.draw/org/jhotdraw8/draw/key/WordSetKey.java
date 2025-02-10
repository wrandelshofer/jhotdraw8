/*
 * @(#)WordSetKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.persistent.PersistentSet;

/**
 * WordSetKey.
 *
 */
public class WordSetKey extends NonNullObjectKey<PersistentSet<String>> {


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public WordSetKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public WordSetKey(String name, PersistentSet<String> defaultValue) {
        super(name, new SimpleParameterizedType(PersistentSet.class, String.class), defaultValue);
    }
}
