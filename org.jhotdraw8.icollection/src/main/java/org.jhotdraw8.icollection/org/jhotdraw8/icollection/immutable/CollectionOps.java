package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.Tuple2;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines operations over immutable collections.
 * <p>
 * This class has been derived from 'vavr' Traversable.java.
 * <dl>
 *     <dt>Traversable.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Traversable.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type of the collection
 * @param <C> the collection type
 */
@SuppressWarnings("unchecked")
public interface CollectionOps<E, C extends CollectionOps<E, C>> extends Iterable<E> {
    /**
     * Removes all elements from this collection that are in the specified collection.
     *
     * @param that the other collection
     * @return a collection with all elements removed that are in that
     */
    C diff(@NonNull ReadOnlyCollection<? super E> that);

    /**
     * Selects all elements of this collection which satisfy the specified predicate.
     *
     * @param p a predicate
     * @return a collection that only contains elements that satisfy the specified predicate
     */
    default C filter(@NonNull Predicate<E> p) {
        CollectionOps<E, C> result = this.<E, C>emptyOp();
        for (E e : this) {
            if (p.test(e)) result = result.add(e);
        }
        return (C) result;
    }

    /**
     * Returns a collection that contains all elements of this collection
     * and one element added to it.
     *
     * @param e an element
     * @return a collection with the specified element added to it.
     */
    C add(E e);

    /**
     * Returns an empty instance.
     *
     * @return empty instance
     */
    <T, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> emptyOp();

    /**
     * Combines all elements of this iterable with the elements from the specified
     * iterable. If one of the iterables is longer than the other, the remaining
     * elements are ignored.
     *
     * @param that    the other collection
     * @param combine the combine function
     * @param <U>     the element type of the other collection
     * @param <T>     the element type of the result collection
     * @param <TC>    the type of the result collection
     * @return the zipped collections
     */
    default <U, T, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> zip(Iterable<U> that, BiFunction<E, U, T> combine) {
        Iterator<E> es = this.iterator();
        Iterator<U> us = that.iterator();
        var result = this.<T, TC>emptyOp();
        while (es.hasNext() && us.hasNext()) {
            E e = es.next();
            U u = us.next();
            result = result.add(combine.apply(e, u));
        }
        return result;
    }

    /**
     * Maps the underlying value to a different component type.
     *
     * @param mapper A mapper
     * @param <T>    The new component type
     * @return A new collection
     */
    default <T, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> map(@NonNull Function<E, T> mapper) {
        var result = this.<T, TC>emptyOp();
        for (E e : this) {
            result = result.add(mapper.apply(e));
        }
        return result;
    }

    /**
     * Gets the first value in iteration order if this {@code Iterable} is not empty, otherwise throws.
     *
     * @return the first value
     * @throws NoSuchElementException if this {@code Iterable} is empty.
     */
    default E get() {
        return iterator().next();
    }

    /**
     * Folds this elements from the left, starting with {@code zero} and successively calling {@code combine}.
     * <p>
     * Example:
     *
     * <pre> {@code
     * // = "cba!"
     * List("a", "b", "c").foldLeft("!", (xs, x) -> x + xs)
     * } </pre>
     *
     * @param <U>     the type to fold over
     * @param zero    A zero element to start with.
     * @param combine A function which combines elements.
     * @return a folded value
     * @throws NullPointerException if {@code combine} is null
     */
    default <U> U foldLeft(U zero, BiFunction<? super U, ? super E, ? extends U> combine) {
        Objects.requireNonNull(combine, "combine is null");
        U xs = zero;
        for (E x : this) {
            xs = combine.apply(xs, x);
        }
        return xs;
    }

    /**
     * Returns the underlying value if present, otherwise {@code other}.
     *
     * @param other An alternative value.
     * @return A value of type {@code T}
     */
    default E getOrElse(E other) {
        return iterator().hasNext() ? other : get();
    }

    /**
     * Calculates the cross product (, i.e. square) of {@code this x this}.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = List of Tuples (1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)
     * List.of(1, 2, 3).crossProduct();
     * </code>
     * </pre>
     *
     * @return a new Iterator containing the square of {@code this}
     */
    default <T extends Tuple2<E, E>, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> crossProduct() {
        return crossProduct(this, (E _1, E _2) -> (T) new Tuple2<E, E>(_1, _2));
    }

    /**
     * Returns {@code true} if this collection contains the specified object.
     *
     * @param o an object
     * @return {@code true} if this collection contains the specified
     * object
     */
    boolean contains(Object o);

    /**
     * Calculates the cross product {@code this x that}.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = List of Tuples (1, 'a'), (1, 'b'), (2, 'a'), (2, 'b'), (3, 'a'), (3, 'b')
     * List.of(1, 2, 3).crossProduct(List.of('a', 'b');
     * </code>
     * </pre>
     *
     * @param that Another iterable
     * @param <U>  Component type
     * @return a new Iterator containing the cross product {@code this x that}
     * @throws NullPointerException if that is null
     */
    default <U, T extends Tuple2<E, U>, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> crossProduct(Iterable<U> that) {
        return crossProduct(that, (E _1, U _2) -> (T) new Tuple2<>(_1, _2));
    }

    /**
     * Calculates the cross product {@code this combine that}.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = List of Tuples (1, 'a'), (1, 'b'), (2, 'a'), (2, 'b'), (3, 'a'), (3, 'b')
     * List.of(1, 2, 3).crossProduct(List.of('a', 'b');
     * </code>
     * </pre>
     *
     * @param that    Another iterable
     * @param combine the combine function
     * @param <U>     Component type
     * @return a new Iterator containing the cross product {@code this x that}
     * @throws NullPointerException if that is null
     */
    default <U, T, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> crossProduct(Iterable<U> that, BiFunction<E, U, T> combine) {
        var result = this.<T, TC>emptyOp();
        for (E e : this) {
            for (U u : that) {
                result = result.add(combine.apply(e, u));
            }
        }
        return result;
    }
}
