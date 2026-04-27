/*
 * @(#)LeafVisitor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.vector;

/// LeafVisitor.
///
/// References:
///
/// This class has been derived from 'vavr' BitMappedTrie.java.
///
/// [vavr BitMappedTrie.java](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/BitMappedTrie.java)
/// [vavr MIT-License](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE)
///
///
/// @param <T> data type
@FunctionalInterface
interface LeafVisitor<T> {
    int visit(int index, T leaf, int start, int end);
}
