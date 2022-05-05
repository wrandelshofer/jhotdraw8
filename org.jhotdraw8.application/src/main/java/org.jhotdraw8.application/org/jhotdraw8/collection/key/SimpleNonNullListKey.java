/*
 * @(#)SimpleNonNullListKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.PersistentArrayList;
import org.jhotdraw8.collection.PersistentList;
import org.jhotdraw8.reflect.TypeToken;

/**
 * An abstract {@link Key} that stores a list of values.
 *
 * @author Werner Randelshofer
 */
public class SimpleNonNullListKey<E> extends SimpleNonNullKey<PersistentList<E>> {

    private static final long serialVersionUID = 1L;

    public SimpleNonNullListKey(String key, TypeToken<PersistentList<E>> type) {
        super(key, type, PersistentArrayList.of());
    }

    public SimpleNonNullListKey(String key, TypeToken<PersistentList<E>> type, @NonNull PersistentList<E> defaultValue) {
        super(key, type, defaultValue);
    }
}
