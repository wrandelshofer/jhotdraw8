/*
 * @(#)ImmutableAddOnlySet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.immutable;

import org.jhotdraw8.annotation.NonNull;

/**
 * Functional Interface for an immutable set that only provides an
 * {@link #add add} method.
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
    @NonNull ImmutableAddOnlySet<E> add(@NonNull E element);

}