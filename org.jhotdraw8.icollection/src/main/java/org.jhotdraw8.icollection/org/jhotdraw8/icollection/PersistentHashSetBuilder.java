package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentHashSet] without
/// generating intermediate editions.
public class PersistentHashSetBuilder<E> implements SetBuilder<E, PersistentHashSet<E>> {
    private BitmapIndexedNode hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public PersistentHashSetBuilder() {
        this(new IdentityObject());
    }

    PersistentHashSetBuilder(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public PersistentHashSetBuilder<E> add(@Nullable E elem) {
        var details = new ChangeEvent();
        hashSet = hashSet.put(owner, elem, new Object[]{elem},
                PersistentHashSet.keyHash(elem), 0, details,
                PersistentHashSet::keepOldEntry,
                PersistentHashSet::keyHash,
                PersistentHashSet.ENTRY_LENGTH);
        size++;
        return this;
    }

    @Override
    public PersistentHashSet<E> build() {
        owner = new IdentityObject();
        return new PersistentHashSet<>(hashSet, size);
    }
}
