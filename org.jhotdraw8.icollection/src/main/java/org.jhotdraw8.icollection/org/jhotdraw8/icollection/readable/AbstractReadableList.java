/*
 * @(#)AbstractReadableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readable;

/**
 * Abstract base class for {@link ReadableList}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadableList<E> extends AbstractReadableCollection<E> implements ReadableList<E> {
    /**
     * Constructs a new instance.
     */
    public AbstractReadableList() {
    }


    @Override
    public boolean equals(Object o) {
        return ReadableList.listEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadableList.iteratorToHashCode(iterator());
    }

}
