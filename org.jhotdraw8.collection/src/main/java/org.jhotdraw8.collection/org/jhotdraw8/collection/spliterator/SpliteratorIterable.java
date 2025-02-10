/*
 * @(#)SpliteratorIterable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.spliterator;


import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An Iterable which takes a Spliterator supplier to implement the
 * Iterable methods.
 *
 * @param <T> the element type
 */
public class SpliteratorIterable<T> implements Iterable<T> {
    private final Supplier<Spliterator<T>> factory;

    public SpliteratorIterable(Supplier<Spliterator<T>> factory) {
        this.factory = factory;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        factory.get().forEachRemaining(action);
    }

    @Override
    public Iterator<T> iterator() {
        return Spliterators.iterator(factory.get());
    }

    @Override
    public Spliterator<T> spliterator() {
        return factory.get();
    }
}
