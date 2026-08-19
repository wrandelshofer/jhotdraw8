package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jspecify.annotations.Nullable;

/// This Builder allows to efficiently build a [PersistentLinkedHashSetWithNodeSubClasses] without
/// generating intermediate editions.
public class PersistentLinkedHashSetBuilderWithNodeSubClasses<E> implements SetBuilder<E, PersistentLinkedHashSetWithNodeSubClasses<E>> {
    private BitmapIndexedNode hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public PersistentLinkedHashSetBuilderWithNodeSubClasses() {
        this(new IdentityObject());
    }

    PersistentLinkedHashSetBuilderWithNodeSubClasses(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public PersistentLinkedHashSetBuilderWithNodeSubClasses<E> add(@Nullable E elem) {
        var details = new ChangeEvent();
        hashSet = hashSet.put(owner, elem, new Object[]{elem, null, null},
                PersistentLinkedHashSetWithNodeSubClasses.keyHash(elem), 0, details,
                PersistentLinkedHashSetWithNodeSubClasses::keepOldEntry,
                PersistentLinkedHashSetWithNodeSubClasses::keyHash,
                PersistentLinkedHashSetWithNodeSubClasses.DATA_LENGTH);
        size++;
        return this;
    }

    @Override
    public PersistentLinkedHashSetWithNodeSubClasses<E> build() {
        owner = new IdentityObject();
        return new PersistentLinkedHashSetWithNodeSubClasses<>(hashSet, size);
    }
}
