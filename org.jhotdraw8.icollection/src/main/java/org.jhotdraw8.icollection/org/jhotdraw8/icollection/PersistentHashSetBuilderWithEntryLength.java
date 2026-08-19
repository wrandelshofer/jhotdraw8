package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentHashSetWithEntryLength] without
/// generating intermediate editions.
public class PersistentHashSetBuilderWithEntryLength<E> implements SetBuilder<E, PersistentHashSetWithEntryLength<E>> {
    private BitmapIndexedNode hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public PersistentHashSetBuilderWithEntryLength() {
        this(new IdentityObject());
    }

    PersistentHashSetBuilderWithEntryLength(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public PersistentHashSetBuilderWithEntryLength<E> add(@Nullable E elem) {
        var details = new ChangeEvent();
        hashSet = hashSet.put(owner, elem, new Object[]{elem},
                PersistentHashSetWithEntryLength.keyHash(elem), 0, details,
                PersistentHashSetWithEntryLength::keepOldEntry,
                PersistentHashSetWithEntryLength::keyHash,
                PersistentHashSetWithEntryLength.ENTRY_LENGTH);
        size++;
        return this;
    }

    @Override
    public PersistentHashSetWithEntryLength<E> build() {
        owner = new IdentityObject();
        return new PersistentHashSetWithEntryLength<>(hashSet, size);
    }
}
