/*
 * @(#)PreorderEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.tree;

import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.SingletonEnumeratorSpliterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * PreorderEnumerator.
 *
 * @author Werner Randelshofer
 */
public class PreorderEnumeratorSpliterator<T> extends AbstractEnumeratorSpliterator<T> {
    private final Function<T, EnumeratorSpliterator<T>> getChildrenFunction;
    private final Deque<EnumeratorSpliterator<T>> stack = new ArrayDeque<>();

    public PreorderEnumeratorSpliterator(Function<T, EnumeratorSpliterator<T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        SingletonEnumeratorSpliterator<T> e = new SingletonEnumeratorSpliterator<>(root);
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
