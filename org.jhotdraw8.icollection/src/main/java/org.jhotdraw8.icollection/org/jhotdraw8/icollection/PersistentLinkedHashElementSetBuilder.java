package org.jhotdraw8.icollection;

import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentLinkedHashElementSet] without
/// generating intermediate editions.
public class PersistentLinkedHashElementSetBuilder<E> implements SetBuilder<E, PersistentLinkedHashElementSet<E>> {
    private PersistentLinkedHashElementSet<E> linkedSet = PersistentLinkedHashElementSet.of();

    public PersistentLinkedHashElementSetBuilder() {

    }

    @Override
    public PersistentLinkedHashElementSetBuilder<E> add(@Nullable E elem) {
        linkedSet = linkedSet.adding(elem);
        return this;
    }

    @Override
    public PersistentLinkedHashElementSet<E> build() {
        return linkedSet;
    }
}
