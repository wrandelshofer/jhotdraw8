package org.jhotdraw8.icollection.impl.redblack;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
   G E N E R A T O R   C R A F T E D
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/


import java.util.Objects;

/**
 * A tuple of 4 elements which can be seen as cartesian product of 4 components.
 *
 * @param <T1> type of the 1st element
 * @param <T2> type of the 2nd element
 * @param <T3> type of the 3rd element
 * @param <T4> type of the 4th element
 */
public final class Tuple4<T1, T2, T3, T4> {

    /**
     * The 1st element of this tuple.
     */
    @SuppressWarnings("serial")
    public final T1 _1;

    /**
     * The 2nd element of this tuple.
     */
    @SuppressWarnings("serial")
    public final T2 _2;

    /**
     * The 3rd element of this tuple.
     */
    @SuppressWarnings("serial")
    public final T3 _3;

    /**
     * The 4th element of this tuple.
     */
    @SuppressWarnings("serial")
    public final T4 _4;

    /**
     * Constructs a tuple of 4 elements.
     *
     * @param t1 the 1st element
     * @param t2 the 2nd element
     * @param t3 the 3rd element
     * @param t4 the 4th element
     */
    public Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
        this._1 = t1;
        this._2 = t2;
        this._3 = t3;
        this._4 = t4;
    }

    public T1 _1() {
        return _1;
    }

    public T2 _2() {
        return _2;
    }

    public T3 _3() {
        return _3;
    }

    public T4 _4() {
        return _4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
        return Objects.equals(_1, tuple4._1) && Objects.equals(_2, tuple4._2) && Objects.equals(_3, tuple4._3) && Objects.equals(_4, tuple4._4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2, _3, _4);
    }

    @Override
    public String toString() {
        return "Tuple4{" +
                "_1=" + _1 +
                ", _2=" + _2 +
                ", _3=" + _3 +
                ", _4=" + _4 +
                '}';
    }
}