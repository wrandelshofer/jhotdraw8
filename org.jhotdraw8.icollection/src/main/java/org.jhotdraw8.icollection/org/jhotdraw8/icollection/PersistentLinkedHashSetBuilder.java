package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentLinkedHashSet] without
/// generating intermediate editions.
public class PersistentLinkedHashSetBuilder<E> implements SetBuilder<E, PersistentLinkedHashSet<E>> {
    private BitmapIndexedNode hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public PersistentLinkedHashSetBuilder() {
        this(new IdentityObject());
    }

    PersistentLinkedHashSetBuilder(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public PersistentLinkedHashSetBuilder<E> add(@Nullable E elem) {
        var details = new ChangeEvent();
        hashSet = hashSet.put(owner, elem, new Object[]{elem, null, null},
                PersistentLinkedHashSet.keyHash(elem), 0, details,
                PersistentLinkedHashSet::keepOldEntry,
                PersistentLinkedHashSet::keyHash,
                PersistentLinkedHashSet.DATA_LENGTH);
        size++;
        return this;
    }

    @Override
    public PersistentLinkedHashSet<E> build() {
        owner = new IdentityObject();
        return new PersistentLinkedHashSet<>(hashSet, size);
    }
}
