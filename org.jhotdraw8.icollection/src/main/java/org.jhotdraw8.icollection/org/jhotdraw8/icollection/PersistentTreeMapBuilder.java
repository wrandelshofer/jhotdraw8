package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;

import java.util.Comparator;

/// This Builder allows to efficiently build a [PersistentTreeSet] without
/// generating intermediate editions.
public class PersistentTreeMapBuilder<K, V> implements MapBuilder<K, V, PersistentTreeMap<K, V>> {
    private RedBlackTree<K, V> tree;
    private final Comparator<K> comparator;

    public PersistentTreeMapBuilder(Comparator<K> comparator) {
        this.tree = RedBlackTree.of(comparator);
        this.comparator = comparator;
    }

    public PersistentTreeMapBuilder() {
        this(NaturalComparator.<K>instance());
    }

    public PersistentTreeMapBuilder<K, V> add(K key, V value) {
        tree = tree.insert(key, value, comparator);
        return this;
    }

    @Override
    public PersistentTreeMap<K, V> build() {
        return new PersistentTreeMap<>(comparator, tree);
    }
}
