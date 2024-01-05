package org.jhotdraw8.icollection.impl.redblack;

import java.util.Objects;

public final class Tuple2<T1, T2> {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public T1 _1() {
        return _1;
    }

    public T2 _2() {
        return _2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Tuple2) obj;
        return Objects.equals(this._1, that._1) &&
                Objects.equals(this._2, that._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        return "Tuple2[" +
                "_1=" + _1 + ", " +
                "_2=" + _2 + ']';
    }

}
