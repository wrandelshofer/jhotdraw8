/*
 * @(#)Pair.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.Nullable;

/**
 * Read-only interface for a pair of values -
 * the pair can be ordered or unordered.
 *
 * @param <U> the type of the first element
 * @param <V> the type of the second element
 * @author Werner Randelshofer
 */
public interface Pair<U, V> {

    @Nullable U first();

    @Nullable V second();
}
