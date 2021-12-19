package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

class PersistentTrie6SetTest extends AbstractPersistentSetTest {

    @Override
    protected PersistentSet<HashCollider> of() {
        return PersistentTrie6Set.of();
    }

    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return PersistentTrie6Set.of(keys);
    }

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return PersistentTrie6Set.copyOf(set);
    }

}