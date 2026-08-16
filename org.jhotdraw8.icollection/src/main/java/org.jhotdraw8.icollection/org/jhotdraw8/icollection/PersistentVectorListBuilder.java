package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.fingertree.FingerTree;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeBuilder;

/// This Builder allows to efficiently build a [PersistentVectorList] without
/// generating intermediate editions.
public class PersistentVectorListBuilder<T> implements ListBuilder<T, PersistentVectorList<T>> {
    private final FingerTreeBuilder<T> b = new FingerTreeBuilder<>();

    @Override
    public PersistentVectorListBuilder<T> add(T elem) {
        b.addOne(elem);
        return this;
    }

    @Override
    public PersistentVectorListBuilder<T> addAll(Iterable<? extends T> elements) {
        if (elements instanceof FingerTree<? extends T> pvl) {
            if (b.isEmpty()) b.initFrom(pvl);
            else b.addVector(pvl);
        } else if (elements instanceof MutableVectorList<? extends T> pvl) {
            if (b.isEmpty()) b.initFrom((FingerTree<?>) pvl.tree);
            else b.addVector((FingerTree<? extends T>) pvl.tree);
        } else {
            b.addAll(elements);
        }
        return this;
    }

    /// Adds the specified element repeated times.
    ///
    /// If the vector is empty when this method is called, then this results
    /// in a sparse vector.
    public PersistentVectorListBuilder<T> addRepeated(T elem, int size) {
        if (b.isEmpty()) b.initSparse(size, elem);
        else for (int i = 0; i < size; i++) {
            add(elem);
        }
        return this;
    }

    @Override
    public PersistentVectorList<T> build() {
        return b.build();
    }
}
