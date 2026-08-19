package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentHashSetWithNodeSubClasses] without
/// generating intermediate editions.
public class PersistentHashSetBuilderWithNodeSubClasses<E> implements SetBuilder<E, PersistentHashSetWithNodeSubClasses<E>> {
    private BitmapIndexedNode<E> hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner;
    private int size;

    public PersistentHashSetBuilderWithNodeSubClasses() {
        this(new IdentityObject());
    }

    PersistentHashSetBuilderWithNodeSubClasses(IdentityObject owner) {
        this.owner = owner;
    }

    @Override
    public PersistentHashSetBuilderWithNodeSubClasses<E> add(@Nullable E elem) {
        var details = new ChangeEvent<E>();
        hashSet = hashSet.put(owner, elem,
                PersistentHashSetWithNodeSubClasses.keyHash(elem), 0, details,
                PersistentHashSetWithNodeSubClasses::keepOldElement,
                Objects::equals, PersistentHashSetWithNodeSubClasses::keyHash);
        size++;
        return this;
    }

    @Override
    public PersistentHashSetWithNodeSubClasses<E> build() {
        owner = new IdentityObject();
        return new PersistentHashSetWithNodeSubClasses<>(hashSet, size);
    }
}
