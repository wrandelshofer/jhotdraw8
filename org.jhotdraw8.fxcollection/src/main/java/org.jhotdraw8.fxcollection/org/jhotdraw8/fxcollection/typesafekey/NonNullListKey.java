/*
 * @(#)NonNullListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.lang.reflect.Type;

/**
 * A {@link Key} that stores a list of values.
 *
 * @param <E> the element type of the list
 */
public class NonNullListKey<E> extends SimpleNonNullKey<PersistentList<E>> {


    public NonNullListKey(String key, Type elementType) {
        super(key, new SimpleParameterizedType(PersistentList.class, elementType), VectorList.of());
    }

    public NonNullListKey(String key, Type elementType, PersistentList<E> defaultValue) {
        super(key, new SimpleParameterizedType(PersistentList.class, elementType), defaultValue);
    }
}
