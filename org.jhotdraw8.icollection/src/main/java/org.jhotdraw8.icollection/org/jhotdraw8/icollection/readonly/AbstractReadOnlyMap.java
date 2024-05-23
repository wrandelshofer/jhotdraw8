/*
 * @(#)AbstractReadOnlyMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readonly;


/**
 * Abstract base class for {@link ReadOnlyMap}s.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractReadOnlyMap<K, V> implements ReadOnlyMap<K, V> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadOnlyMap() {
    }

    public boolean equals(Object o) {
        return ReadOnlyMap.mapEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iteratorToHashCode(iterator());
    }

    @Override
    public final String toString() {
        return ReadOnlyMap.mapToString(this);
    }
}
