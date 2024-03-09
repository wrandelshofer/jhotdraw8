/*
 * @(#)Empty.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

/**
 * The empty tree node.
 * <p>
 * This class has been derived from 'vavr' RedBlackTree.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/RedBlackTree.java">github.com</a></dd>
 * </dl>
 *
 * @param <K> Component type
 */
public final class Empty<K, V> implements RedBlackTree<K, V> {
    public final static Empty<?, ?> EMPTY = new Empty<>();

    @SuppressWarnings("unchecked")
    public static <K, V> Empty<K, V> empty() {
        return (Empty<K, V>) EMPTY;
    }

    private Empty() {
    }

    @Override
    public boolean color() {
        return Color.BLACK;
    }

    @Override
    public boolean contains(K key, Comparator<? super K> comparator) {
        return false;
    }


    @Override
    public RedBlackTree<K, V> find(K key, Comparator<? super K> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<K, V> ceiling(K e, @NonNull Comparator<? super K> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<K, V> floor(K e, @NonNull Comparator<? super K> comparator) {
        return this;
    }

    @Override
    public @NonNull RedBlackTree<K, V> higher(K e, @NonNull Comparator<? super K> comparator) {
        return this;
    }

    @Override
    public RedBlackTree<K, V> orElse(RedBlackTree<K, V> other) {
        return other;
    }

    @Override
    public @NonNull RedBlackTree<K, V> lower(K e, @NonNull Comparator<? super K> comparator) {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public RedBlackTree<K, V> left() {
        throw new UnsupportedOperationException("left on empty");
    }

    @Override
    public @NonNull RedBlackTree<K, V> right() {
        throw new UnsupportedOperationException("right on empty");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public K getKey() {
        throw new NoSuchElementException("key on empty");
    }

    @Override
    public V getValue() {
        throw new NoSuchElementException("value on empty");
    }

    @Override
    public @Nullable K keyOrNull() {
        return null;
    }

    @Override
    public @Nullable V valueOrNull() {
        return null;
    }

    @Override
    public <E> @Nullable E mapOrNull(@NonNull BiFunction<K, V, E> f) {
        return null;
    }

    @Override
    public Map.@Nullable Entry<K, V> entryOrNull() {
        return null;
    }

    @Override
    public boolean isRed() {
        return false;
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public String toLispString() {
        return "()";
    }
}
