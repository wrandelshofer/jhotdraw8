package org.jhotdraw8.icollection.impl.redblack;

public class Tuple {
    /**
     * Don't let anyone instantiate this class.
     */
    private Tuple() {
    }


    public static <T1, T2> Tuple2<T1, T2> of(T1 _1, T2 _2) {
        return new Tuple2<>(_1, _2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 _1, T2 _2, T3 _3) {
        return new Tuple3<>(_1, _2, _3);
    }
}
