package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import java.io.Serial;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyAppend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyInit;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyUpdate;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty4;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap3;

///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public final class Tree3<A> extends FingerTree<A> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final int size;
    private final byte len1;
    private final short len12;
    private final A[] p1;
    private final A[][] p2;
    private final A[][][] d3;
    private final A[][] s2;
    private final A[] s1;

    /**
     *
     */
    public Tree3(int size, byte len1, short len12, A[] p1, A[][] p2, A[][][] d3, A[][] s2,
                 A[] s1) {
        this.size = size;
        this.len1 = len1;
        this.len12 = len12;
        this.p1 = p1;
        this.p2 = p2;
        this.d3 = d3;
        this.s2 = s2;
        this.s1 = s1;
    }

    public Tree3(int size, int len1, int len12, A[] p1, A[][] p2, A[][][] d3, A[][] s2, A[] s1) {
        this(size, (byte) len1, (short) len12, p1, p2, d3, s2, s1);
    }

    public Tree3(int size, A[] p1, A[][] p2, A[][][] d3, A[][] s2, A[] s1) {
        var len1 = p1.length;
        var len12 = len1 + p2.length * WIDTH;
        this(size, (byte) len1, (short) len12, p1, p2, d3, s2, s1);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public @Nullable A getFirst() {
        return p1[0];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @Nullable A getLast() {
        return s1[s1.length - 1];
    }

    @Override
    public FingerTree<A> addingLast(A x) {
        if (s1.length < WIDTH)
            return new Tree3<>(size + 1, len1, len12, p1, p2, d3, s2, copyAppend(s1, x));
        if (s2.length < WIDTH - 1)
            return new Tree3<>(size + 1, len1, len12, p1, p2, d3, copyAppend(s2, s1), wrap1(x));
        if (d3.length < WIDTH - 2)
            return new Tree3<>(size + 1, len1, len12, p1, p2, copyAppend(d3, copyAppend(s2, s1)), empty2(), wrap1(x));

        return new Tree4<>(size + 1, len1, len12, (WIDTH - 2) * WIDTH2 + len12,
                p1, p2, d3, empty4(), wrap3(copyAppend(s2, s1)), empty2(), wrap1(x));

    }

    @Override
    public int getSliceCount() {
        return 5;
    }

    public A[] getSliceAt(int idx) {
        //noinspection unchecked
        return (A[]) switch (idx) {
            case 0 -> p1;
            case 1 -> p2;
            case 2 -> d3;
            case 3 -> s2;
            case 4 -> s1;
            default -> throw new IllegalArgumentException("Unexpected value: " + idx);
        };
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        if (s1.length > 1)
            return new FingerTreeAPI.Result<>(new Tree3<>(size - 1, len1, len12, p1, p2, d3, s2, copyInit(s1)), s1[s1.length - 1]);
        else return new FingerTreeAPI.Result<>(slice(0, size - 1), s1[0]);
    }

    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        if (len1 > 1)
            return new FingerTreeAPI.Result<>(new Tree3<>(size - 1, len1 - 1, len12 - 1, copyTail(p1), p2, d3, s2, s1), p1[0]);
        else return new FingerTreeAPI.Result<>(slice(1, size), p1[0]);
    }

    @Override
    public FingerTree<A> slice(int lo, int hi) {
        var b = new SliceBuilder<A>(lo, hi);
        b.consider(1, p1);
        b.consider(2, p2);
        b.consider(3, d3);
        b.consider(2, s2);
        b.consider(1, s1);
        return b.result();
    }

    @Override
    public FingerTree<A> addingFirst(A x) {
        if (len1 < WIDTH)
            return new Tree3<>(size + 1, len1 + 1, len12 + 1,//
                    copyPrepend(x, p1), p2, d3, s2, s1);
        if (len12 < WIDTH2)
            return new Tree3<>(size + 1, 1, len12 + 1,//
                    wrap1(x), copyPrepend(p1, p2), d3, s2, s1);
        if (d3.length < WIDTH - 2)
            return new Tree3<>(size + 1, 1, 1,//
                    wrap1(x), empty2(), copyPrepend(copyPrepend(p1, p2), d3), s2, s1);

        return new Tree4<>(size + 1, 1, 1, len12 + 1,
                wrap1(x), empty2(), wrap3(copyPrepend(p1, p2)), empty4(), d3, s2, s1);

    }

    @Override
    public @Nullable A get(int index) {
        if (len1 > index) return p1[index];
        if (len12 > index) {
            var io = index - len1;
            return p2[io >>> BITS][io & MASK];
        }
        var io = index - len12;
        var i3 = io >>> BITS2;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i3 < d3.length) return d3[i3][i2][i1];
        if (i2 < s2.length) return s2[i2][i1];
        return s1[i1];
    }

    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        if (len1 > index) {
            return new FingerTreeAPI.Result<>(new Tree3<>(size, len1, len12, copyUpdate(p1, index, x), p2, d3, s2, s1), p1[index]);
        }
        if (len12 > index) {
            var io = index - len1;
            int i2 = io >>> BITS;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree3<>(size, len1, len12, p1, copyUpdate(p2, i2, i1, x), d3, s2, s1), p2[i2][i1]);
        }
        var io = index - len12;
        var i3 = io >>> BITS2;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i3 < d3.length)
            return new FingerTreeAPI.Result<>(new Tree3<>(size, len1, len12, p1, p2, copyUpdate(d3, i3, i2, i1, x), s2, s1), d3[i3][i2][i1]);
        if (i2 < s2.length)
            return new FingerTreeAPI.Result<>(new Tree3<>(size, len1, len12, p1, p2, d3, copyUpdate(s2, i2, i1, x), s1), s2[i2][i1]);
        return new FingerTreeAPI.Result<>(new Tree3<>(size, len1, len12, p1, p2, d3, s2, copyUpdate(s1, i1, x)), s1[i1]);
    }

    @Override
    public int size() {
        return size;
    }

    byte len1() {
        return len1;
    }

    short len12() {
        return len12;
    }

    A[] p1() {
        return p1;
    }

    A[][] p2() {
        return p2;
    }

    A[][][] d3() {
        return d3;
    }

    A[][] s2() {
        return s2;
    }

    A[] s1() {
        return s1;
    }
}
