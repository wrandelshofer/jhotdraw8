/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.IdentityObject;

/**
 * A {@link BitmapIndexedNode} that provides storage space for a 'owner' identity.
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
class MutableBitmapIndexedNode<K> extends BitmapIndexedNode<K> {

    private final IdentityObject ownedBy;

    MutableBitmapIndexedNode(IdentityObject ownedBy, int nodeMap, int dataMap, Object[] nodes) {
        super(nodeMap, dataMap, nodes);
        this.ownedBy = ownedBy;
    }

    @Override
    protected IdentityObject getOwner() {
        return ownedBy;
    }
}
