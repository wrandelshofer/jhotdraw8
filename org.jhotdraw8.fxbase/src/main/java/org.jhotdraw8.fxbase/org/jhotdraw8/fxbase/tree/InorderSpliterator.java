/*
 * @(#)InorderSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * InorderSpliterator.
 *
 * @param <T> the element type
 * @author Werner Randelshofer
 */
public class InorderSpliterator<T> extends AbstractSpliterator<T> {
    private final Function<T, Iterable<T>> getChildrenFunction;
    private @Nullable T root;
    private Spliterator<T> subtree;
    private final Iterator<T> children;

    public InorderSpliterator(Function<T, Iterable<T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        this.getChildrenFunction = getChildrenFunction;
        this.root = root;
        children = getChildrenFunction.apply(root).iterator();
        if (children.hasNext()) {
            subtree = new InorderSpliterator<>(getChildrenFunction, children.next());
        } else {
            subtree = Spliterators.emptySpliterator();
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (root == null) {
            return false;
        }

        //noinspection StatementWithEmptyBody
        if (subtree.tryAdvance(action)) {
            // empty
        } else if (children.hasNext()) {
            subtree = new InorderSpliterator<>(getChildrenFunction, children.next());
            subtree.tryAdvance(action);
        } else {
            action.accept(root);
            root = null;
        }
        return true;
    }
}
