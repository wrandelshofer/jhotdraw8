/*
 * @(#)AbstractReadOnlySet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

public abstract class AbstractReadOnlySet<E> extends AbstractReadOnlyCollection<E> implements ReadOnlySet<E> {
    public AbstractReadOnlySet() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ReadOnlySet)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ReadOnlyCollection<E> c = (ReadOnlyCollection<E>) o;
        if (c.size() != size()) {
            return false;
        }
        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(this.iterator());
    }
}
