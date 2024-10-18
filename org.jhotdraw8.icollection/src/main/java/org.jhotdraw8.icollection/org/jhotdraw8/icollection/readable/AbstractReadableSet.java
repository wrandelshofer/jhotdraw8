/*
 * @(#)AbstractReadableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readable;

/**
 * Abstract base class for {@link ReadableSet}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadableSet<E> extends AbstractReadableCollection<E> implements ReadableSet<E> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadableSet() {
    }

    @Override
    public boolean equals(Object o) {
        return ReadableSet.setEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(this.iterator());
    }
}
