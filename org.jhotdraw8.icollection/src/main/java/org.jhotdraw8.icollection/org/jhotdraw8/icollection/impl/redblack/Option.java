/*
 * @(#)Option.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;


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

    public record Some<T>(T value) implements Option<T> {

    }

    public record None<T>() implements Option<T> {
        /**
         * The singleton instance of None.
         */
        private static final None<?> INSTANCE = new None<>();
    }
}
