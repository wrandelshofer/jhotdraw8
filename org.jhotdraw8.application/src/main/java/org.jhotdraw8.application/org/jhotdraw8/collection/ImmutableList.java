/*
 * @(#)ImmutableList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Read-only interface for an immutable list; the implementation guarantees that
 * the state of the list does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableList<E> extends ReadOnlyList<E>, ImmutableCollection<E> {
    @NonNull
    ImmutableList<E> readOnlySubList(int fromIndex, int toIndex);

}
