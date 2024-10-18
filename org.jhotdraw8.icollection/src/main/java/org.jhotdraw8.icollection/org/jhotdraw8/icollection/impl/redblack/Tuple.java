package org.jhotdraw8.icollection.impl.redblack;

/**
 * A Tuple combines a fixed number of elements together so that they can be passed around as a whole.
 * Unlike an array or list, a tuple can hold objects with different types, but they are also persistent.
 * <p>
 * This class has been derived from 'vavr' Tuple.java.
 * <dl>
 *     <dt>Tuple.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src-gen/main/java/io/vavr/Tuple.java">github.com</a></dd>
 * </dl>
 */
public interface Tuple {


    static <T1, T2> Tuple2<T1, T2> of(T1 _1, T2 _2) {
        return new Tuple2<>(_1, _2);
    }

    static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 _1, T2 _2, T3 _3) {
        return new Tuple3<>(_1, _2, _3);
    }

    static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 _1, T2 _2, T3 _3, T4 _4) {
        return new Tuple4<>(_1, _2, _3, _4);
    }
}
