/*
 * @(#)AbstractReadOnlyList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.pcollection.readonly;

/**
 * Abstract base class for {@link ReadOnlyList}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadOnlyList<E> extends AbstractReadOnlyCollection<E> implements ReadOnlyList<E> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadOnlyList() {
    }


    @Override
    public boolean equals(Object o) {
        return ReadOnlyList.listEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlyList.iteratorToHashCode(iterator());
    }

}
