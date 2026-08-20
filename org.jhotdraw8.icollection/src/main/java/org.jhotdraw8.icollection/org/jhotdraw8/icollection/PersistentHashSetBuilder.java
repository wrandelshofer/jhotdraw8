package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.champset.TrieBuilder;
import org.jhotdraw8.icollection.impl.champset.TrieNode;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentHashSet] without
/// generating intermediate editions.
public class PersistentHashSetBuilder<E> implements SetBuilder<E, PersistentHashSet<E>> {
    private TrieNode<E> node = TrieNode.empty();
    private TrieBuilder<E> builder;

    public PersistentHashSetBuilder() {
        this(new MutabilityOwnership());
    }

    PersistentHashSetBuilder(MutabilityOwnership owner) {
        this.builder = new TrieBuilder<>(owner);
    }

    @Override
    public PersistentHashSetBuilder<E> add(@Nullable E elem) {
        node = node.mutableAdd(Objects.hashCode(elem), elem, 0, builder);
        return this;
    }

    @Override
    public PersistentHashSet<E> build() {
        builder.ownership = new MutabilityOwnership();
        return new PersistentHashSet<>(node, builder.size);
    }
}
