/*
 * @(#)PersistentTrieSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

public class WrappedPersistentSetTest extends AbstractPersistentSetTest {


    @Override
    protected PersistentSet<HashCollider> of() {
        return WrappedPersistentSet.of();
    }


    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return WrappedPersistentSet.of(keys);
    }

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return WrappedPersistentSet.copyOf(set);
    }
}