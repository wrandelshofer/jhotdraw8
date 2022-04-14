/*
 * @(#)PersistentTrieSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

public class PersistentTrieSetTest extends AbstractPersistentSetTest {

    @Override
    protected PersistentSet<HashCollider> of() {
        return PersistentTrieSet.of();
    }

    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return PersistentTrieSet.of(keys);
    }

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return PersistentTrieSet.copyOf(set);
    }
}