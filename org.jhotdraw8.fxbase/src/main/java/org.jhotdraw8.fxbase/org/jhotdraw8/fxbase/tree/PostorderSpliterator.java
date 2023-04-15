/*
 * @(#)PostorderSpliterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * PreorderSpliterator.
 *
 * @param <T> the element type
 * @author Werner Randelshofer
 */
public class PostorderSpliterator<T> extends AbstractSpliterator<T> {
    private final @NonNull Function<T, Iterable<T>> getChildrenFunction;
    private @Nullable T root;
    private Spliterator<T> subtree;
    private final Iterator<T> children;

    public PostorderSpliterator(@NonNull Function<T, Iterable<T>> getChildrenFunction, @NonNull T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        this.getChildrenFunction = getChildrenFunction;
        this.root = root;
        children = getChildrenFunction.apply(root).iterator();
        subtree = Spliterators.emptySpliterator();
    }

    @Override
    public boolean tryAdvance(@NonNull Consumer<? super T> consumer) {
        if (root == null) {
            return false;
        }

        //noinspection StatementWithEmptyBody
        if (subtree.tryAdvance(consumer)) {
            // empty
        } else if (children.hasNext()) {
            subtree = new PostorderSpliterator<>(getChildrenFunction, children.next());
            subtree.tryAdvance(consumer);
        } else {
            consumer.accept(root);
            root = null;
        }
        return true;
    }
}
