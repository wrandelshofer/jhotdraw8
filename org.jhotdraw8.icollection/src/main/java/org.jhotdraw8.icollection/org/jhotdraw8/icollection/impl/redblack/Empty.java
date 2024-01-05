/*
 * @(#)Empty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * The empty tree node. It can't be a singleton because it depends on a {@link Comparator}.
 * <p>
 * This class has been derived from 'vavr' RedBlackTree.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/RedBlackTree.java">github.com</a></dd>
 * </dl>
 *
 * @param <T> Component type
 */
public final class Empty<T> implements RedBlackTree<T> {


    final Comparator<T> comparator;

    // This is no public API! The RedBlackTree takes care of passing the correct Comparator.
    @SuppressWarnings("unchecked")
    Empty(Comparator<? super T> comparator) {
        this.comparator = (Comparator<T>) comparator;
    }

    @Override
    public Color color() {
        return Color.BLACK;
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public boolean contains(T value) {
        return false;
    }

    @Override
    public Empty<T> emptyInstance() {
        return this;
    }

    @Override
    public Option<T> find(T value) {
        return Option.none();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public RedBlackTree<T> left() {
        throw new UnsupportedOperationException("left on empty");
    }

    @Override
    public RedBlackTree<T> right() {
        throw new UnsupportedOperationException("right on empty");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public T value() {
        throw new NoSuchElementException("value on empty");
    }

    @Override
    public String toString() {
        return "()";
    }
}
