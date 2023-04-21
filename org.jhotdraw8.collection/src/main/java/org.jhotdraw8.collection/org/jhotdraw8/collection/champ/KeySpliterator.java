/*
 * @(#)KeySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.function.Function;

/**
 * Key iterator over a CHAMP trie.
 * <p>
 * Uses a stack with a fixed maximal depth.
 * Iterates over keys in preorder sequence.
 * <p>
 * Supports the {@code remove} operation. The remove function must
 * create a new version of the trie, so that iterator does not have
 * to deal with structural changes of the trie.
 */
class KeySpliterator<K, E> extends AbstractKeySpliterator<K, E> {
    KeySpliterator(@NonNull Node<K> root, @Nullable Function<K, E> mappingFunction, int characteristics, long size) {
        super(root, mappingFunction, characteristics, size);
    }

    KeySpliterator(@NonNull Node<K> root) {
        super(root, null, 0, Long.MAX_VALUE);
    }

    @Override
    boolean isReverse() {
        return false;
    }

    @Override
    int getNextBitpos(StackElement<K> elem) {
        return 1 << Integer.numberOfTrailingZeros(elem.map);
    }

    @Override
    boolean isDone(@NonNull StackElement<K> elem) {
        return elem.index >= elem.size;
    }

    @Override
    int moveIndex(@NonNull StackElement<K> elem) {
        return elem.index++;
    }

}
