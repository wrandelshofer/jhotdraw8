/*
 * @(#)Empty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import org.jhotdraw8.annotation.NonNull;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * The empty tree node.
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
    public final static Empty<?> EMPTY = new Empty<>();

    @SuppressWarnings("unchecked")
    public static <T> Empty<T> empty() {
        return (Empty<T>) EMPTY;
    }

    private Empty() {
    }

    @Override
    public boolean color() {
        return Color.BLACK;
    }

    @Override
    public boolean contains(T value, Comparator<? super T> comparator) {
        return false;
    }


    @Override
    public RedBlackTree<T> find(T value, Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<T> ceiling(T e, @NonNull Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<T> floor(T e, @NonNull Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<T> higher(T e, @NonNull Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public RedBlackTree<T> orElse(RedBlackTree<T> other) {
        return other;
    }

    @Override
    public @NonNull RedBlackTree<T> lower(T e, @NonNull Comparator<? super T> comparator) {
        return this;
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
    public T orNull() {
        return null;
    }

    @Override
    public String toString() {
        return "()";
    }
}
