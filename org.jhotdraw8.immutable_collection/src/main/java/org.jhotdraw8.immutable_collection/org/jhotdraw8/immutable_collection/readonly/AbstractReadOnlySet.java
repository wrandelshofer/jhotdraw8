/*
 * @(#)AbstractReadOnlySet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.immutable_collection.readonly;

/**
 * Abstract base class for {@link ReadOnlySet}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadOnlySet<E> extends AbstractReadOnlyCollection<E> implements ReadOnlySet<E> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadOnlySet() {
    }

    @Override
    public boolean equals(Object o) {
        return ReadOnlySet.setEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(this.iterator());
    }
}
