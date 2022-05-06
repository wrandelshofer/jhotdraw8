/*
 * @(#)Sets.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;
import java.util.Set;

/**
 * Static utility methods for sets.
 */
public class Sets {

    /**
     * Don't let anyone instantiate this class.
     */
    private Sets() {
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    public static <E> Set<E> addAll(@NonNull Set<E> set, E... elements) {
        set.addAll(Arrays.asList(elements));
        return set;
    }
}
