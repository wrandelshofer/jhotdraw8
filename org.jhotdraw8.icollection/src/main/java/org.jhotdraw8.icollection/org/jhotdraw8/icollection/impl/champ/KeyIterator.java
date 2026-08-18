/*
 * @(#)BaseTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/// Entry iterator over a CHAMP trie.
///
/// Uses a fixed stack in depth.
/// Iterates first over inlined data entries and then continues depth first.
public class KeyIterator<K, V> extends AbstractChampIterator implements Iterator<K> {
    @Nullable K current;
    protected final @Nullable Consumer<K> persistentRemoveFunction;

    /// Creates a new instance.
    ///
    /// @param rootNode                 the root node of the trie
    /// @param persistentRemoveFunction a function that removes an entry from a field;
    ///                                       the function must not change the trie that was passed
    ///                                       to this iterator
    /// @param ENTRY_LENGTH
    public KeyIterator(Node rootNode, @Nullable Consumer<K> persistentRemoveFunction, int ENTRY_LENGTH) {
        super(rootNode, ENTRY_LENGTH);
        this.persistentRemoveFunction = persistentRemoveFunction;
    }

    @Override
    public @Nullable K next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            canRemove = true;
            current = (K) nextValueNode.getKey(nextValueCursor++, ENTRY_LENGTH);
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
        K toRemove = current;
        persistentRemoveFunction.accept(toRemove);
        canRemove = false;
        current = null;
    }
}
