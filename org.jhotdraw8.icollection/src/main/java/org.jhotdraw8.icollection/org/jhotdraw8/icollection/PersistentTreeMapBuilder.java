package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;

import java.util.AbstractMap;
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
        RedBlackTree<K, V> newRoot = tree.insert(key, value, comparator);
        if (newRoot == tree) {
            throw new IllegalStateException("There is already an entry with this key in the map. key=" + key);
        }
        tree = newRoot;
        return this;
    }

    @Override
    public PersistentTreeMap<K, V> build() {
        return new PersistentTreeMap<>(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(comparator, tree)));
    }
}
