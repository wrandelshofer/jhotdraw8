/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IdentityObject;

/**
 * A {@link ChampBitmapIndexedNode} that provides storage space for a 'mutator' identity.
 * <p>
 * References:
 * <p>
 * This class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K>
 */
class ChampMutableBitmapIndexedNode<K> extends ChampBitmapIndexedNode<K> {
    private static final long serialVersionUID = 0L;
    private final @NonNull IdentityObject mutator;

    ChampMutableBitmapIndexedNode(@NonNull IdentityObject mutator, int nodeMap, int dataMap, @NonNull Object @NonNull [] nodes) {
        super(nodeMap, dataMap, nodes);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull IdentityObject getMutator() {
        return mutator;
    }
}
