package org.jhotdraw8.collection.transform;

/**
 * Interface for objects that can be transformed by a function using
 * a fluent API.
 * <p>
 * Sample implementation:
 * <pre>{@literal
 * class Sample implements Transformable {
 *
 *   @Override
 *   public Transformer<Sample> transformed() {
 *     return this::transform; // method reference
 *   }
 *
 *   private <R> R transform(Function<? super Sample, ? extends R> f) {
 *     return f.apply(this);
 *   }
 * }
 * }</pre>
 * Usage:
 * <pre>{@literal
 *    var obj = new Sample();
 *    Function<Sample,String> func = obj::toString;
 *
 *    var result = obj.transformed().by(func);
 * }</pre>
 * If the function returns a {@code Transformable}, the API can
 * be used in a fluent style:
 * <pre>{@literal
 *    var obj = new Sample();
 *    Function<Sample, Sample> func = Sample::removeFirst;
 *
 *    var result = obj.transformed().by(func).transformed().by(func);
 * }</pre>
 * <p>
 * Note: The fluent style would be more elegant, if Java provided
 * a self-type. In this case, the {@code Transformable}
 * interface could be simplified to this:
 * <pre>{@literal
 * interface Transformable {
 *    <T> T transform(Function<self-type, T> f);
 * }
 * }</pre>
 * <p>
 * References:
 * <dl>
 *      <dt>Tomasz Linkowski (2018).
 *      Transformer Pattern.</dt>
 *      <dd><a href="https://blog.tlinkowski.pl/2018/transformer-pattern/">blog.tlinkowski.pl</a>
 * </dl>
 */
public interface Transformable {
    /**
     * Returns a {@link Transformer} with self type.
     * <p>
     * Usage:
     * <pre>{@literal
     * obj.transformed().by(function);
     * }</pre>
     *
     * @return transformer with self type
     */
    Transformer<?> transformed();
}
