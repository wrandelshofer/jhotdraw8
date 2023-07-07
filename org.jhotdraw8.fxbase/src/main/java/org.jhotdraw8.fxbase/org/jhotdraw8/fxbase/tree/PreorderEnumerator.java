/*
 * @(#)PreorderEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.spliterator.SingletonSpliterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * PreorderEnumerator.
 *
 * @param <T> the element type
 * @author Werner Randelshofer
 */
public class PreorderEnumerator<T> extends AbstractEnumerator<T> {
    private final Function<T, EnumeratorSpliterator<T>> getChildrenFunction;
    private final Deque<EnumeratorSpliterator<T>> stack = new ArrayDeque<>();

    public PreorderEnumerator(Function<T, EnumeratorSpliterator<T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        SingletonSpliterator<T> e = new SingletonSpliterator<>(root);
        e.moveNext();
        stack.push(e);
        this.getChildrenFunction = getChildrenFunction;
    }

    @Override
    public boolean moveNext() {
        EnumeratorSpliterator<T> iter = stack.peek();
        if (iter == null) {
            return false;
        }

        current = iter.current();
        if (!iter.moveNext()) {
            stack.pop();
        }
        EnumeratorSpliterator<T> children = getChildrenFunction.apply(current);
        if (children.moveNext()) {
            stack.push(children);
        }
        return true;
    }
}
