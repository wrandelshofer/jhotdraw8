package org.jhotdraw8.collection.transform;

import java.util.function.Function;

/**
 * Interface of the transformer with proper type parameters.
 * <p>
 * References:
 * <dl>
 *      <dt>Tomasz Linkowski (2018).
 *      Transformer Pattern.</dt>
 *      <dd><a href="https://blog.tlinkowski.pl/2018/transformer-pattern/">blog.tlinkowski.pl</a>
 * </dl>
 *
 * @param <T> the type of the object that is to be transformed
 */
@FunctionalInterface
public interface Transformer<T> {
    /**
     * Transforms a {@link Transformable} object with the specified
     * function.
     *
     * @param f   a function
     * @param <R> the result type of the function
     * @return the result of the function
     */
    <R> R by(Function<? super T, ? extends R> f);
}