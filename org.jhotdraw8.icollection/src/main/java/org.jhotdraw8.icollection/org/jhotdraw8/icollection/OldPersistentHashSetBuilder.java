package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// This Builder allows to efficiently build a [OldPersistentHashSet] without
/// generating intermediate editions.
public class OldPersistentHashSetBuilder<E> implements SetBuilder<E, OldPersistentHashSet<E>> {
    private BitmapIndexedNode<E> hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public OldPersistentHashSetBuilder() {
        this(new IdentityObject());
    }

    OldPersistentHashSetBuilder(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public OldPersistentHashSetBuilder<E> add(@Nullable E elem) {
        var details = new ChangeEvent<E>();
        hashSet = hashSet.put(owner, elem,
                OldPersistentHashSet.keyHash(elem), 0, details,
                OldPersistentHashSet::keepOldElement,
                Objects::equals, OldPersistentHashSet::keyHash);
        size++;
        return this;
    }

    @Override
    public OldPersistentHashSet<E> build() {
        owner = new IdentityObject();
        return new OldPersistentHashSet<>(hashSet, size);
    }
}
