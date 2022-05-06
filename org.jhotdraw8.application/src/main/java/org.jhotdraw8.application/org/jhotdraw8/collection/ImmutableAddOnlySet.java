/*
 * @(#)ImmutableAddOnlySet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Interface for an immutable set that only provides a {@code copyAdd} method.
 *
 * @param <E> the element type
 */
public interface ImmutableAddOnlySet<E> {
    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set if it already contains the element, or
     * a different set with the element added
     */
    @NonNull ImmutableAddOnlySet<E> copyAdd(@NonNull E element);

}
