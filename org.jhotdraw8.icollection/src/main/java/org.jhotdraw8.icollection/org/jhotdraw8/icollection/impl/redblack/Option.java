/*
 * @(#)Option.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;


import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Replacement for {@link Optional}.
 * <p>
 * Option is a <a href="http://stackoverflow.com/questions/13454347/monads-with-java-8">monadic</a> container type which
 * represents an optional value. Instances of Option are either an instance of {@link Some} or the
 * singleton {@link None}.
 * <p>
 * Most of the API is taken from {@link Optional}. A similar type can be found in <a
 * href="http://hackage.haskell.org/package/base-4.6.0.1/docs/Data-Maybe.html">Haskell</a> and <a
 * href="http://www.scala-lang.org/api/current/#scala.Option">Scala</a>.
 * <p>
 * This class has been derived from 'vavr' Option.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/control/Option.java">github.com</a></dd>
 * </dl>
 *
 * @param <T> The type of the optional value.
 */
public interface Option<T> {
    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return (Option<T>) None.INSTANCE;
    }

    static <T> Option<T> some(T value) {
        return new Some<>(value);
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
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return this;
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
        public boolean isEmpty() {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return (Option<T>) other;
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
