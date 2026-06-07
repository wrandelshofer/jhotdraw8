/*
 * @(#)PostorderSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.collection.enumerator.EmptyEnumerator;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

/// PostorderSpliterator.
///
/// @param <T> the element type
public class PostorderSpliterator<T> extends AbstractEnumerator<T> {
    private final Function<T, Iterable<T>> getChildrenFunction;
    private @Nullable T root;
    private Enumerator<T> subtree;
    private final Iterator<T> children;

    public PostorderSpliterator(Function<T, Iterable<T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        this.getChildrenFunction = getChildrenFunction;
        this.root = root;
        children = getChildrenFunction.apply(root).iterator();
        subtree = EmptyEnumerator.emptyEnumerator();
    }

    @Override
    public boolean moveNext() {
        if (root == null) {
            return false;
        }
        if (subtree.moveNext()) {
            current = subtree.current();
        } else if (children.hasNext()) {
            subtree = new PostorderSpliterator<>(getChildrenFunction, children.next());
            subtree.moveNext();
            current = subtree.current();
        } else {
            current = root;
            root = null;
        }
        return true;
    }

}
