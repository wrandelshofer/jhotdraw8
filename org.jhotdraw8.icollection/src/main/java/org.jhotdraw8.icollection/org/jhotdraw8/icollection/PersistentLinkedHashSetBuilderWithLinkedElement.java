package org.jhotdraw8.icollection;

import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentLinkedHashSetWithLinkedElement] without
/// generating intermediate editions.
public class PersistentLinkedHashSetBuilderWithLinkedElement<E> implements SetBuilder<E, PersistentLinkedHashSetWithLinkedElement<E>> {
    private PersistentLinkedHashSetWithLinkedElement<E> linkedSet = PersistentLinkedHashSetWithLinkedElement.of();

    public PersistentLinkedHashSetBuilderWithLinkedElement() {

    }

    @Override
    public PersistentLinkedHashSetBuilderWithLinkedElement<E> add(@Nullable E elem) {
        linkedSet = linkedSet.adding(elem);
        return this;
    }

    @Override
    public PersistentLinkedHashSetWithLinkedElement<E> build() {
        return linkedSet;
    }
}
