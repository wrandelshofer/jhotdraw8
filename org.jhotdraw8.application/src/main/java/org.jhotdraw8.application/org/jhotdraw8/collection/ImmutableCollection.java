/*
 * @(#)ImmutableCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

/**
 * Read-only interface for an immutable collection; the implementation
 * guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableCollection<E> extends ReadOnlyCollection<E> {
}
