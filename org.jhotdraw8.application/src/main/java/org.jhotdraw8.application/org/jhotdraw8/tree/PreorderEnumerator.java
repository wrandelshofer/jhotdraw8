/*
 * @(#)PreorderEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.tree;

import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.SingletonEnumerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * PreorderEnumerator.
 *
 * @author Werner Randelshofer
 */
public class PreorderEnumerator<T> extends AbstractEnumerator<T> {
    private final Function<T, Enumerator<T>> getChildrenFunction;
    private final Deque<Enumerator<T>> stack = new ArrayDeque<>();

    public PreorderEnumerator(Function<T, Enumerator<T>> getChildrenFunction, T root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        SingletonEnumerator<T> e = new SingletonEnumerator<>(root);
        e.moveNext();
        stack.push(e);
        this.getChildrenFunction = getChildrenFunction;
    }

    @Override
    public boolean moveNext() {
        Enumerator<T> iter = stack.peek();
        if (iter == null) {
            return false;
        }

        current = iter.current();
        if (!iter.moveNext()) {
            stack.pop();
        }
        Enumerator<T> children = getChildrenFunction.apply(current);
        if (children.moveNext()) {
            stack.push(children);
        }
        return true;
    }
}
