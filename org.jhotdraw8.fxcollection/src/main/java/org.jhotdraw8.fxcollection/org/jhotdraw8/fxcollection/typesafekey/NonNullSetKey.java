/*
 * @(#)NonNullListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.lang.reflect.Type;

/**
 * A {@link Key} that stores a set of values.
 * <p>
 * FIXME This class is trivial. We should use {@link SimpleNonNullKey} directly.
 *
 * @param <E> the element type of the set
 */
public class NonNullSetKey<E> extends SimpleNonNullKey<PersistentSet<E>> {


    public NonNullSetKey(String key, Type elementType) {
        super(key, new SimpleParameterizedType(PersistentSet.class, elementType), ChampSet.of());
    }

    public NonNullSetKey(String key, Type elementType, PersistentSet<E> defaultValue) {
        super(key, new SimpleParameterizedType(PersistentSet.class, elementType), defaultValue);
    }
}
