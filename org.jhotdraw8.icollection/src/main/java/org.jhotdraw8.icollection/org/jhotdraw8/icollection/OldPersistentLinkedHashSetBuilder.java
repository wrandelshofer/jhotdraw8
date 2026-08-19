package org.jhotdraw8.icollection;

import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [OldPersistentLinkedHashSet] without
/// generating intermediate editions.
public class OldPersistentLinkedHashSetBuilder<E> implements SetBuilder<E, OldPersistentLinkedHashSet<E>> {
    private OldPersistentLinkedHashSet<E> linkedSet = OldPersistentLinkedHashSet.of();

    public OldPersistentLinkedHashSetBuilder() {

    }

    @Override
    public OldPersistentLinkedHashSetBuilder<E> add(@Nullable E elem) {
        linkedSet = linkedSet.adding(elem);
        return this;
    }

    @Override
    public OldPersistentLinkedHashSet<E> build() {
        return linkedSet;
    }
}
