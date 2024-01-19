/*
 * @(#)Option.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.CollectionOps;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An Option is a read-only collection of one element or of zero elements.
 * <p>
 * Instances of Option are either an instance of {@link Some} or of {@link None}.
 * <p>
 * This class has been derived from 'vavr' Option.java.
 * <dl>
 *     <dt>Option.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/control/Option.java">github.com</a></dd>
 * </dl>
 *
 * @param <T> The type of the optional value.
 */
public interface Option<T> extends ReadOnlyCollection<T>, CollectionOps<T, Option<T>> {
    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return (Option<T>) None.INSTANCE;
    }

    static <T> Option<T> some(T value) {
        return new Some<>(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T1, TC extends CollectionOps<T1, TC>> CollectionOps<T1, TC> emptyOp() {
        return (CollectionOps<T1, TC>) Option.none();
    }


    /**
     * Creates a new {@code Option} of a given value.
     *
     * <pre>{@code
     * // = Some(3), an Option which contains the value 3
     * Option<Integer> option = Option.of(3);
     *
     * // = None, the empty Option
     * Option<Integer> none = Option.of(null);
     * }</pre>
     *
     * @param value A value
     * @param <T>   type of the value
     * @return {@code Some(value)} if value is not {@code null}, {@code None} otherwise
     */
    static <T> Option<T> of(T value) {
        return (value == null) ? none() : some(value);
    }
    record Some<T>(T value) implements Option<T> {
        @Override
        public Option<T> diff(@NonNull ReadOnlyCollection<? super T> that) {
            return that.contains(value) ? none() : this;
        }

        @Override
        public Option<T> filter(@NonNull Predicate<T> p) {
            return p.test(value) ? none() : this;
        }

        @Override
        public Option<T> add(T t) {
            throw new IllegalStateException("Some can not contain more than one element.");
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return Objects.equals(value, o);
        }

        @Override
        public @NonNull Iterator<T> iterator() {
            return Set.of(value).iterator();
        }

        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return this;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orNull() {
            return value;
        }

        @Override
        public T orThrow() {
            return value;
        }
    }

    record None<T>() implements Option<T> {
        /**
         * The singleton instance of None.
         */
        private static final None<?> INSTANCE = new None<>();

        @Override
        public None<T> diff(@NonNull ReadOnlyCollection<? super T> that) {
            return this;
        }

        @Override
        public None<T> filter(@NonNull Predicate<T> p) {
            return this;
        }

        @Override
        public Option<T> add(T t) {
            return new Some<>(t);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public @NonNull Iterator<T> iterator() {
            return Collections.emptyIterator();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return (Option<T>) other;
        }

        @Override
        public T get() {
            throw new NoSuchElementException();
        }

        @Override
        public T orNull() {
            return null;
        }

        @Override
        public T orThrow() {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns true, if this is {@code None}, otherwise false, if this is {@code Some}.
     *
     * <pre>{@code
     * // Prints "false"
     * System.out.println(Option.of(10).isEmpty());
     *
     * // Prints "true"
     * System.out.println(Option.none().isEmpty());
     * }</pre>
     *
     * @return true, if this {@code Option} is empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Returns this {@code Option} if it is nonempty, otherwise return the alternative.
     *
     * <pre>{@code
     * Option<String> other = Option.of("Other");
     *
     * // = Some("Hello World")
     * Option.of("Hello World").orElse(other);
     *
     * // = Some("Other")
     * Option.none().orElse(other);
     * }</pre>
     *
     * @param other An alternative {@code Option}
     * @return this {@code Option} if it is nonempty, otherwise return the alternative.
     */
    @SuppressWarnings("unchecked")
    Option<T> orElse(Option<? extends T> other);

    /**
     * Returns the value if this is a {@code Some} or the {@code other} value if this is a {@code None}.
     * <p>
     * Please note, that the other value is eagerly evaluated.
     *
     * <pre>{@code
     * // Prints "Hello"
     * System.out.println(Option.of("Hello").getOrElse("World"));
     *
     * // Prints "World"
     * Option.none().getOrElse("World");
     * }</pre>
     *
     * @param other An alternative value
     * @return This value, if this Option is defined or the {@code other} value, if this Option is empty.
     */
    default T getOrElse(T other) {
        return isEmpty() ? other : get();
    }

    /**
     * Gets the value if this is a {@code Some} or throws if this is a {@code None}.
     *
     * <pre>{@code
     * // Prints "57"
     * System.out.println(Option.of(57).get());
     *
     * // Throws a NoSuchElementException
     * Option.none().get();
     * }</pre>
     *
     * @return the value
     * @throws NoSuchElementException if this is a {@code None}.
     */

    T get();
    /**
     * Returns this {@code Option} if this is defined, or {@code null} if it is empty.
     *
     * <pre>{@code
     * // = Some("Hello World")
     * Option.of("Hello World").orNull();
     *
     * // = null
     * Option.none().orNull();
     * }</pre>
     *
     * @return this value if it is defined, or {@code null} if it is empty.
     */
    T orNull();

    /**
     * Returns this {@code Option} if this is defined, or throws a {@code NoSuchElementException} if it is empty.
     *
     * <pre>{@code
     * // = Some("Hello World")
     * Option.of("Hello World").orThrow();
     *
     * // = null
     * Option.none().orThrow();
     * }</pre>
     *
     * @return this value if it is defined, or {@code null} if it is empty.
     */
    T orThrow();

}
