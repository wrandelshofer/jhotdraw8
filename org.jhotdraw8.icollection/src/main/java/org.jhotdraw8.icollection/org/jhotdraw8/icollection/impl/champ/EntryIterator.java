/*
 * @(#)BaseTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/// Entry iterator over a CHAMP trie.
///
/// Uses a fixed stack in depth.
/// Iterates first over inlined data entries and then continues depth first.
///
/// Supports remove and [Map.Entry#setValue]. The functions that are
/// passed to this iterator must not change the trie structure that the iterator
/// currently uses.
public class EntryIterator<K, V> extends AbstractChampIterator implements Iterator<Map.Entry<K, V>> {

    @Nullable EditableMapEntry<K, V> current;
    protected final @Nullable Consumer<K> persistentRemoveFunction;

    protected final @Nullable BiConsumer<K, V> persistentPutIfPresentFunction;

    /// Creates a new instance.
    ///
    /// @param rootNode                       the root node of the trie
    /// @param persistentRemoveFunction       a function that removes an entry from a field;
    ///                                       the function must not change the trie that was passed
    ///                                       to this iterator
    /// @param persistentPutIfPresentFunction a function that replaces the value of an entry;
    ///                                       the function must not change the trie that was passed
    ///                                       to this iterator
    /// @param ENTRY_LENGTH
    public EntryIterator(Node rootNode, @Nullable Consumer<K> persistentRemoveFunction, @Nullable BiConsumer<K, V> persistentPutIfPresentFunction, int ENTRY_LENGTH) {
        super(rootNode,
                ENTRY_LENGTH);
        this.persistentRemoveFunction = persistentRemoveFunction;
        this.persistentPutIfPresentFunction = persistentPutIfPresentFunction;
    }

    @Override
    public @Nullable EditableMapEntry<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            canRemove = true;
            Object[] entry = nextValueNode.getEntry(nextValueCursor++, ENTRY_LENGTH);
            current = new EditableMapEntry<K, V>((K) entry[0], (V) entry[1], 0);
            current.setPutIfPresentFunction(persistentPutIfPresentFunction);
            return current;
        }
    }


    @Override
    public void remove() {
        if (persistentRemoveFunction == null) {
            throw new UnsupportedOperationException("remove");
        }
        if (!canRemove || current == null) {
            throw new IllegalStateException();
        }
        Map.Entry<K, V> toRemove = current;
        persistentRemoveFunction.accept(toRemove.getKey());
        canRemove = false;
        current = null;
    }
}
