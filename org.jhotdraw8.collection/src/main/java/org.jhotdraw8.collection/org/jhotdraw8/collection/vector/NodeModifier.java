/*
 * @(#)NodeModifier.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.vector;

import static org.jhotdraw8.collection.vector.ArrayType.obj;

/**
 * NodeModifier.
 * <p>
 * References:
 * <p>
 * This class has been derived from Vavr BitMappedTrie.java.
 * <dl>
 *     <dt>BitMappedTrie.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/BitMappedTrie.java">github.com</a></dd>
 * </dl>
 */
@FunctionalInterface
interface NodeModifier {
    Object apply(Object array, int index);

    NodeModifier COPY_NODE = (o, i) -> obj().copy(o, i + 1);
    NodeModifier IDENTITY = (o, i) -> o;
}
