package org.jhotdraw8.collection.immutable;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides 'zip' transformations.
 */
public class ZipTransformer {
    /**
     * Zips a list with itself.
     * <p>
     * Example:
     * VectorList<String> list = VectorList.of("a","b","c");
     * VectorList<String> result = list.transformed().by(Zipper.zipList((a,b)->a+b));
     * // result = ["ab","c"];
     * </p>
     *
     * @param combinator the combinator
     * @param <E>        the element type
     * @param <T>        the list type
     * @return the zipped list
     */
    @SuppressWarnings("unchecked")
    public static <E, T extends ImmutableList<E>> Function<? super T, T> zipList(BiFunction<E, E, E> combinator) {
        return c -> {
            var result = c.clear();
            Iterator<E> i = c.iterator();
            while (i.hasNext()) {
                E a = i.next();
                if (i.hasNext()) {
                    E b = i.next();
                    result = result.add(combinator.apply(a, b));
                } else {
                    result = result.add(a);
                }
            }
            return (T) result;
        };
    }

    /**
     * Zips a list with itself in reverse order.
     * <p>
     * Example:
     * VectorList<String> list = VectorList.of("a","b","c");
     * VectorList<String> result = list.transformed().by(Zipper.zipListInReverse((a,b)->a+b));
     * // result = ["a","bc"];
     * </p>
     *
     * @param combinator the combinator
     * @param <E>        the element type
     * @param <T>        the list type
     * @return the zipped list
     */
    @SuppressWarnings("unchecked")
    public static <E, T extends ImmutableList<E>> Function<? super T, T> zipListInReverse(BiFunction<E, E, E> combinator) {
        return c -> {
            var result = c.clear();
            Iterator<E> i = c.reversed().iterator();
            while (i.hasNext()) {
                E a = i.next();
                if (i.hasNext()) {
                    E b = i.next();
                    result = result.addFirst(combinator.apply(b, a));
                } else {
                    result = result.addFirst(a);
                }
            }
            return (T) result;
        };
    }
}
