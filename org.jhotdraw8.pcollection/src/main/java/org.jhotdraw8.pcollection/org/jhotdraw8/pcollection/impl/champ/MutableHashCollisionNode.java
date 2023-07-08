/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.champ;

import org.jhotdraw8.annotation.NonNull;

/**
 * A {@link HashCollisionNode} that provides storage space for a 'owner' identity.
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
class MutableHashCollisionNode<K> extends HashCollisionNode<K> {
    private static final long serialVersionUID = 0L;
    private final @NonNull IdentityObject ownedBy;

    MutableHashCollisionNode(@NonNull IdentityObject ownedBy, int hash, Object @NonNull [] entries) {
        super(hash, entries);
        this.ownedBy = ownedBy;
    }

    @Override
    protected @NonNull IdentityObject getOwner() {
        return ownedBy;
    }
}
