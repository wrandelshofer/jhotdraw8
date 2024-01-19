package org.jhotdraw8.icollection.euler;


import org.jhotdraw8.icollection.Option;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.SimpleImmutableSet;
import org.jhotdraw8.icollection.Tuple3;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.impl.iteration.IntRangeIterator;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Static utility methods for computing a sieve of prime numbers.
 * <p>
 * This class has been derived from 'vavr' Sieve.java.
 * <dl>
 *     <dt>Sieve.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/test/java/io/vavr/collection/euler/Sieve.java">github.com</a></dd>
 * </dl>
 */
public class Sieve {
    /**
     * Don't let anyone instantiate this class.
     */
    private Sieve() {
    }

    private final static ImmutableList<BiFunction<Integer, Integer, Option<Integer>>> RULES =
            SimpleImmutableList.of(
                    (x, y) -> Option.of((4 * x * x) + (y * y)).filter(n -> n % 12 == 1 || n % 12 == 5),
                    (x, y) -> Option.of((3 * x * x) + (y * y)).filter(n -> n % 12 == 7),
                    (x, y) -> Option.of((3 * x * x) - (y * y)).filter(n -> x > y && n % 12 == 11)
            );

    private final static ImmutableList<Function<Tuple3<ImmutableSet<Integer>, Integer, Integer>, ImmutableSet<Integer>>> STEPS =
            SimpleImmutableList.of(
                    (Tuple3<ImmutableSet<Integer>, Integer, Integer> sieveLimitRoot) -> SimpleImmutableSet.ofIterator(IntStream.rangeClosed(1, sieveLimitRoot._3()).iterator()).crossProduct()
                            .foldLeft(sieveLimitRoot._1(),
                                    (xs, xy) ->
                                            RULES.foldLeft(xs, (ss, r) -> r.apply(xy._1(), xy._2())
                                                    .filter(p -> p < sieveLimitRoot._2())
                                                    .map(p -> ss.contains(p) ? ss.remove(p) : ss.add(p))
                                                    .getOrElse(ss)
                                            )
                            ),
                    (Tuple3<ImmutableSet<Integer>, Integer, Integer> sieveLimitRoot) -> SimpleImmutableSet.ofIterator(IntStream.rangeClosed(5, sieveLimitRoot._3()).iterator())
                            .foldLeft(sieveLimitRoot._1(), (xs, r) -> xs.contains(r)
                                    ? SimpleImmutableSet.ofIterator(new IntRangeIterator(r * r, sieveLimitRoot._2(), r * r)).foldLeft(xs, ImmutableSet::remove)
                                    : xs
                            )
            );


}
