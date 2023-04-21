/*
 * @(#)AbstractReadOnlyMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.readonly;

import org.jhotdraw8.annotation.NonNull;

/**
 * Abstract base class for {@link ReadOnlyMap}s.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractReadOnlyMap<K, V> implements ReadOnlyMap<K, V> {
    public AbstractReadOnlyMap() {
    }

    public boolean equals(Object o) {
        return ReadOnlyMap.mapEquals(this,o);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iteratorToHashCode(iterator());
    }

    @Override
    public final @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }
}
