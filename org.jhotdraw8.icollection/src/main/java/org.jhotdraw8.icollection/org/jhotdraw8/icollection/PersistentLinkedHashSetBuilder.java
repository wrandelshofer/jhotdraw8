package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.champlinked.TrieBuilder;
import org.jhotdraw8.icollection.impl.champlinked.TrieNode;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

import static org.jhotdraw8.icollection.PersistentLinkedHashSet.ENTRY_SIZE;
import static org.jhotdraw8.icollection.PersistentLinkedHashSet.KEY_INDEX;
import static org.jhotdraw8.icollection.PersistentLinkedHashSet.NEXT_INDEX;
import static org.jhotdraw8.icollection.PersistentLinkedHashSet.PREV_INDEX;

import static org.jhotdraw8.icollection.impl.champlinked.TrieNode.NO_DATA;

/// This Builder allows to efficiently build a [PersistentLinkedHashSet] without
/// generating intermediate editions.
public class PersistentLinkedHashSetBuilder<E> implements SetBuilder<E, PersistentLinkedHashSet<E>> {
    private TrieNode<E> node = TrieNode.empty();
    private TrieBuilder<E, Object> builder;
    Object[] first = null;
    Object[] delayed = null;
    private int size = 0;

    public PersistentLinkedHashSetBuilder() {
        this(new MutabilityOwnership());
    }

    PersistentLinkedHashSetBuilder(MutabilityOwnership owner) {
        this.builder = new TrieBuilder<>(owner);
    }

    PersistentLinkedHashSetBuilder(TrieBuilder<E, Object> builder) {
        this.builder = builder.reset();
    }

    @Override
    public PersistentLinkedHashSetBuilder<E> addAll(Iterable<? extends E> elements) {
        if (size == 0 && elements instanceof PersistentLinkedHashSet<? extends E> set) {
            if (set.isEmpty()) {
                return this;
            }
            builder.size = set.size();
            node = (TrieNode<E>) set.node;
            first = set.first.clone();
            delayed = set.last.clone();
            size = set.size;
            return this;
        }
        return (PersistentLinkedHashSetBuilder<E>) SetBuilder.super.addAll(elements);
    }

    @Override
    public PersistentLinkedHashSetBuilder<E> add(@Nullable E elem) {
        if (size == 0) {
            // set is empty
            var newEntry = new Object[ENTRY_SIZE];
            newEntry[KEY_INDEX] = elem;
            newEntry[PREV_INDEX] = NO_DATA;
            newEntry[NEXT_INDEX] = NO_DATA;
            delayed = newEntry;
            first = delayed;
            size++;
            return this;
        }
        int keyHash = Objects.hashCode(elem);
        if (Objects.equals(delayed[KEY_INDEX], NO_DATA)
                || node.containsKey(keyHash, elem, 0, ENTRY_SIZE)) {
            // elem is already in set
            return this;
        }
        delayed[NEXT_INDEX] = elem;
        node = node.mutablePut(Objects.hashCode(delayed[KEY_INDEX]), (E) delayed[KEY_INDEX], delayed, 0, builder,
                PersistentLinkedHashSet::updReplaceWithNewEntry, ENTRY_SIZE);
        if (size == 1) {
            first = delayed.clone();
        }

        delayed[PREV_INDEX] = delayed[KEY_INDEX];
        delayed[KEY_INDEX] = elem;
        delayed[NEXT_INDEX] = NO_DATA;
        size++;
        return this;
    }

    @Override
    public PersistentLinkedHashSet<E> build() {
        if (size == 0) {
            return PersistentLinkedHashSet.of();
        }
        BiFunction<Object[], Object[], Object[]> updateFunction = PersistentLinkedHashSet::updReplaceWithNewEntry;
        var newNode = node.mutablePut(Objects.hashCode(delayed[KEY_INDEX]), (E) delayed[KEY_INDEX], delayed,
                0, new TrieBuilder<E, Object>(), updateFunction, ENTRY_SIZE);
        builder.ownership = new MutabilityOwnership();
        return new PersistentLinkedHashSet<>(newNode, size, first, delayed);
    }
}
