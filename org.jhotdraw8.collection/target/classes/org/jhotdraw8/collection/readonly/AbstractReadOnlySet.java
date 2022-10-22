/*
 * @(#)AbstractReadOnlySet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.readonly;

/**
 * Abstract base class for {@link ReadOnlySet}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadOnlySet<E> extends AbstractReadOnlyCollection<E> implements ReadOnlySet<E> {
    public AbstractReadOnlySet() {
    }

    @Override
    public boolean equals(Object o) {
        return ReadOnlySet.setEquals(this,o);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(this.iterator());
    }
}
