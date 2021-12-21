/*
 * @(#)PersistentSets.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Provides factory methods for persistent sets.
 */
public class PersistentSets {
    /**
     * Don't let anyone instantiate this class.
     */
    public PersistentSets() {
    }


    @SuppressWarnings("unchecked")
    public static @NonNull <T> PersistentSet<T> of() {
        return PersistentTrieSet.of();
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static @NonNull <T> PersistentSet<T> of(@NonNull T... items) {
        return PersistentTrieSet.of(items[0]);
    }

    public static @NonNull <T> PersistentSet<T> copyOf(@NonNull Iterable<T> collection) {
        return PersistentTrieSet.copyOf(collection);
    }
}
