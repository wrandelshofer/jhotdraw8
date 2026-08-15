package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentHashSet] without
/// generating intermediate editions.
public class PersistentHashSetBuilder<E> implements SetBuilder<E, PersistentHashSet<E>> {
    private BitmapIndexedNode<E> hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private int size;

    @Override
    public PersistentHashSetBuilder<E> add(@Nullable E elem) {
        var details = new ChangeEvent<E>();
        hashSet = hashSet.put(owner, elem,
                PersistentHashSet.keyHash(elem), 0, details,
                PersistentHashSet::insertOrFail,
                Objects::equals, PersistentHashSet::keyHash);
        size++;
        return this;
    }

    @Override
    public PersistentHashSet<E> build() {
        owner = new IdentityObject();
        return new PersistentHashSet<>(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(hashSet, size)));
    }
}
