package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.champset.SequencedElement;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeBuilder;

import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentVectorHashSet] without
/// generating intermediate editions.
public class PersistentVectorHashSetBuilder<E> implements SetBuilder<E, PersistentVectorHashSet<E>> {
    private final FingerTreeBuilder<Object> vector = new FingerTreeBuilder<>();
    private BitmapIndexedNode<SequencedElement<E>> hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private final int offset;
    private int size;

    public PersistentVectorHashSetBuilder() {
        this(new IdentityObject(), Integer.MIN_VALUE / 4);
    }

    PersistentVectorHashSetBuilder(IdentityObject owner, int offset) {
        this.offset = offset;
        this.owner = owner;
    }

    @Override
    public PersistentVectorHashSetBuilder<E> add(E elem) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(elem, size + offset);
        var newHashSet = hashSet.put(owner, newElem,
                SequencedElement.keyHash(elem), 0, details,
                SequencedElement::keepOldValue,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            hashSet = newHashSet;
            vector.addOne(newElem);
            size++;
        }
        return this;
    }


    @Override
    public PersistentVectorHashSet<E> build() {
        owner = new IdentityObject();
        return new PersistentVectorHashSet<>(hashSet, vector.build(), size, offset);
    }
}
