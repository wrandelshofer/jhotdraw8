package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.SequencedElement;
import org.jhotdraw8.icollection.impl.fingertree.FingerTree;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeBuilder;
import org.jhotdraw8.icollection.impl.fingertree.Tree0;

import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentVectorHashSet] without
/// generating intermediate editions.
public class PersistentVectorHashSetBuilder<E> implements SetBuilder<E, PersistentVectorHashSet<E>> {
    private final FingerTreeBuilder<Object> vector = new FingerTreeBuilder<>();
    private FingerTree<SequencedElement<E>> x = Tree0.empty();
    private BitmapIndexedNode<SequencedElement<E>> hashSet = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private final int offset;
    private int size;

    public PersistentVectorHashSetBuilder() {
        this(Integer.MIN_VALUE / 4);
    }

    PersistentVectorHashSetBuilder(int offset) {
        this.offset = offset;
    }

    @Override
    public PersistentVectorHashSetBuilder<E> add(E elem) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(elem, size + offset);
        hashSet = hashSet.put(owner, newElem,
                SequencedElement.keyHash(elem), 0, details,
                SequencedElement::insertOrFail,
                Objects::equals, SequencedElement::elementKeyHash);
        vector.addOne(newElem);
        size++;
        return this;
    }


    @Override
    public PersistentVectorHashSet<E> build() {
        owner = new IdentityObject();
        return new PersistentVectorHashSet<>(new PrivateData(
                new PersistentVectorHashSet.OpaqueRecord<>(hashSet,
                        vector.build(), size, offset)));
    }
}
