package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readable.ReadableSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.SortedSet;

/// This Builder allows to efficiently build a [PersistentTreeSet] without
/// generating intermediate editions.
public class PersistentTreeSetBuilder<E> implements SetBuilder<E, PersistentTreeSet<E>> {
    private RedBlackTree<E, Void> tree;
    private @Nullable Comparator<E> comparator;

    public PersistentTreeSetBuilder(Comparator<E> comparator) {
        this.tree = RedBlackTree.of(comparator);
        this.comparator = comparator;
    }

    public PersistentTreeSetBuilder() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public PersistentTreeSetBuilder<E> initComparator(Iterable<? extends E> iterable) {
        if (!tree.isEmpty())
            throw new IllegalStateException("Can not init comparator because tree is not empty anymore.");
        comparator = (Comparator<E>) switch (iterable) {
            case SortedSet<? extends E> ss -> ss.comparator();
            case ReadableSortedSet<? extends E> ss -> ss.comparator();
            default -> null;
        };
        return this;
    }

    @Override
    public PersistentTreeSetBuilder<E> add(E elem) {
        RedBlackTree<E, Void> newRoot = tree.insert(elem, null, getComparatorOrDefault());
        if (newRoot == tree) {
            throw new IllegalStateException("Element is already in the set. elem=" + elem);
        }
        tree = newRoot;
        return this;
    }

    private Comparator<E> getComparatorOrDefault() {
        return comparator == null ? NaturalComparator.<E>instance() : comparator;
    }


    @Override
    public PersistentTreeSet<E> build() {
        return new PersistentTreeSet<>(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(getComparatorOrDefault(), tree)));
    }
}
