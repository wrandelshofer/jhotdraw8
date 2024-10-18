/*
 * @(#)AbstractReadableMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readable;


/**
 * Abstract base class for {@link ReadableMap}s.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractReadableMap<K, V> implements ReadableMap<K, V> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadableMap() {
    }

    public boolean equals(Object o) {
        return ReadableMap.mapEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadableMap.iteratorToHashCode(iterator());
    }

    @Override
    public final String toString() {
        return ReadableMap.mapToString(this);
    }
}
