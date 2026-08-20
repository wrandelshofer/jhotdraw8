/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champmap;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;

/// A [BitmapIndexedNode] that provides storage space for a 'owner' identity.
///
/// References:
///
/// This class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
/// </dl>
///
/// @param <K>
class MutableBitmapIndexedNode<K> extends BitmapIndexedNode<K> {

    private final MutabilityOwnership ownedBy;

    MutableBitmapIndexedNode(MutabilityOwnership ownedBy, int nodeMap, int dataMap, Object[] nodes) {
        super(nodeMap, dataMap, nodes);
        this.ownedBy = ownedBy;
    }

    @Override
    protected MutabilityOwnership getOwner() {
        return ownedBy;
    }
}
