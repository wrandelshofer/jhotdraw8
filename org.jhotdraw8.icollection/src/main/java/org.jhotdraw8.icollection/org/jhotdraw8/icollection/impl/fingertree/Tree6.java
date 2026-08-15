package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyAppend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyInit;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyUpdate;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty3;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty4;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty5;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;

public record Tree6<A>(int size, byte len1, short len12, short len123, int len1234, int len12345,
                       A[] p1, A[][] p2, A[][][] p3, A[][][][] p4, A[][][][][] p5,
                       A[][][][][][] d6,
                       A[][][][][] s5, A[][][][] s4, A[][][] s3, A[][] s2, A[] s1) implements FingerTree<A> {

    public Tree6(int size, A[] p1, A[][] p2, A[][][] p3, A[][][][] p4, A[][][][][] p5, A[][][][][][] d6, A[][][][][] s5, A[][][][] s4, A[][][] s3, A[][] s2, A[] s1) {
        var len1 = p1.length;
        var len12 = len1 + p2.length * WIDTH;
        var len123 = len12 + p3.length * WIDTH2;
        var len1234 = len123 + p4.length * WIDTH3;
        var len12345 = len1234 + p5.length * WIDTH4;
        this(size, (byte) len1, (short) len12, (short) len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, s1);
    }

    public Tree6(int size, int len1, int len12, int len123, int len1234, int len12345, A[] p1, A[][] p2, A[][][] p3, A[][][][] p4, A[][][][][] p5, A[][][][][][] d6, A[][][][][] s5, A[][][][] s4, A[][][] s3, A[][] s2, A[] s1) {
        this(size, (byte) len1, (short) len12, (short) len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, s1);
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
    public FingerTree<A> addLast(A x) {
        if (s1.length < WIDTH)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, copyAppend(s1, x));
        if (s2.length < WIDTH - 1)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, d6, s5, s4, s3, copyAppend(s2, s1), wrap1(x));
        if (s3.length < WIDTH - 1)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, d6, s5, s4, copyAppend(s3, copyAppend(s2, s1)), empty2(), wrap1(x));
        if (s4.length < WIDTH - 1)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, d6, s5, copyAppend(s4, copyAppend(s3, copyAppend(s2, s1))), empty3(), empty2(), wrap1(x));
        if (s5.length < WIDTH - 1)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, d6, copyAppend(s5, copyAppend(s4, copyAppend(s3, copyAppend(s2, s1)))), empty4(), empty3(), empty2(), wrap1(x));
        if (d6.length < LASTWIDTH - 2)
            return new Tree6<>(size + 1, len1, len12, len123, len1234, len12345,
                    p1, p2, p3, p4, p5, copyAppend(d6, copyAppend(s5, copyAppend(s4, copyAppend(s3, copyAppend(s2, s1))))), empty5(), empty4(), empty3(), empty2(), wrap1(x));
        throw new IllegalStateException("too many elements " + (size + 1));
    }

    @Override
    public FingerTree<A> addFirst(A x) {
        if (len1 < WIDTH)
            return new Tree6<>(size + 1, len1 + 1, len12 + 1, len123 + 1, len1234 + 1, len12345 + 1,
                    copyPrepend(x, p1), p2, p3, p4, p5, d6, s5, s4, s3, s2, s1);
        if (len12 < WIDTH2)
            return new Tree6<>(size + 1, 1, len12 + 1, len123 + 1, len1234 + 1, len12345 + 1,
                    wrap1(x), copyPrepend(p1, p2), p3, p4, p5, d6, s5, s4, s3, s2, s1);
        if (len123 < WIDTH3)
            return new Tree6<>(size + 1, 1, 1, len123 + 1, len1234 + 1, len12345 + 1,
                    wrap1(x), empty2(), copyPrepend(copyPrepend(p1, p2), p3), p4, p5, d6, s5, s4, s3, s2, s1);
        if (len1234 < WIDTH4)
            return new Tree6<>(size + 1, 1, 1, 1, len1234 + 1, len12345 + 1,
                    wrap1(x), empty2(), empty3(), copyPrepend(copyPrepend(copyPrepend(p1, p2), p3), p4), p5, d6, s5, s4, s3, s2, s1);
        if (len12345 < WIDTH5)
            return new Tree6<>(size + 1, 1, 1, 1, 1, len12345 + 1,
                    wrap1(x), empty2(), empty3(), empty4(), copyPrepend(copyPrepend(copyPrepend(copyPrepend(p1, p2), p3), p4), p5), d6, s5, s4, s3, s2, s1);
        if (d6.length < LASTWIDTH - 2)
            return new Tree6<>(size + 1, 1, 1, 1, 1, 1,
                    wrap1(x), empty2(), empty3(), empty4(), empty5(), copyPrepend(copyPrepend(copyPrepend(copyPrepend(copyPrepend(p1, p2), p3), p4), p5), d6), s5, s4, s3, s2, s1);
        throw new IllegalStateException("too many elements " + (size + 1));
    }

    @Override
    public @Nullable A get(int index) {
        if (len1 > index) return p1[index];
        if (len12 > index) {
            var io = index - len1;
            return p2[io >>> BITS][io & MASK];
        }
        if (len123 > index) {
            var io = index - len12;
            return p3[io >>> BITS2][(io >>> BITS) & MASK][io & MASK];
        }
        if (len1234 > index) {
            var io = index - len123;
            return p4[io >>> BITS3][(io >>> BITS2) & MASK][(io >>> BITS) & MASK][io & MASK];
        }
        if (len12345 > index) {
            var io = index - len1234;
            return p5[io >>> BITS4][(io >>> BITS3) & MASK][(io >>> BITS2) & MASK][(io >>> BITS) & MASK][io & MASK];
        }
        var io = index - len12345;
        var i6 = io >>> BITS5;
        var i5 = (io >>> BITS4) & MASK;
        var i4 = (io >>> BITS3) & MASK;
        var i3 = (io >>> BITS2) & MASK;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i6 < d6.length) return d6[i6][i5][i4][i3][i2][i1];
        if (i5 < s5.length) return s5[i5][i4][i3][i2][i1];
        if (i4 < s4.length) return s4[i4][i3][i2][i1];
        if (i3 < s3.length) return s3[i3][i2][i1];
        if (i2 < s2.length) return s2[i2][i1];
        return s1[i1];
    }


    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        if (len1 > index)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, copyUpdate(p1, index, x), p2, p3, p4, p5, d6, s5, s4, s3, s2, s1), p1[index]);
        if (len12 > index) {
            var io = index - len1;
            int i2 = io >>> BITS;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, copyUpdate(p2, i2, i1, x), p3, p4, p5, d6, s5, s4, s3, s2, s1), p2[i2][i1]);
        }
        if (len123 > index) {
            var io = index - len12;
            int i3 = io >>> BITS2;
            int i2 = (io >>> BITS) & MASK;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, copyUpdate(p3, i3, i2, i1, x), p4, p5, d6, s5, s4, s3, s2, s1), p3[i3][i2][i1]);
        }
        if (len1234 > index) {
            var io = index - len123;
            int i4 = io >>> BITS3;
            int i3 = (io >>> BITS2) & MASK;
            int i2 = (io >>> BITS) & MASK;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, copyUpdate(p4, i4, i3, i2, i1, x), p5, d6, s5, s4, s3, s2, s1), p4[i4][i3][i2][i1]);
        }
        if (len12345 > index) {
            var io = index - len1234;
            int i5 = io >>> BITS4;
            int i4 = (io >>> BITS3) & MASK;
            int i3 = (io >>> BITS2) & MASK;
            int i2 = (io >>> BITS) & MASK;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, copyUpdate(p5, i5, i4, i3, i2, i1, x), d6, s5, s4, s3, s2, s1), p5[i5][i4][i3][i2][i1]);
        }
        var io = index - len12345;
        var i6 = io >>> BITS5;
        var i5 = (io >>> BITS4) & MASK;
        var i4 = (io >>> BITS3) & MASK;
        var i3 = (io >>> BITS2) & MASK;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i6 < d6.length)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, copyUpdate(d6, i6, i5, i4, i3, i2, i1, x), s5, s4, s3, s2, s1), d6[i6][i5][i4][i3][i2][i1]);
        if (i5 < s5.length)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, copyUpdate(s5, i5, i4, i3, i2, i1, x), s4, s3, s2, s1), s5[i5][i4][i3][i2][i1]);
        if (i4 < s4.length)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, copyUpdate(s4, i4, i3, i2, i1, x), s3, s2, s1), s4[i4][i3][i2][i1]);
        if (i3 < s3.length)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, copyUpdate(s3, i3, i2, i1, x), s2, s1), s3[i3][i2][i1]);
        if (i2 < s2.length)
            return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, s3, copyUpdate(s2, i2, i1, x), s1), s2[i2][i1]);
        return new FingerTreeAPI.Result<>(new Tree6<>(size, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, copyUpdate(s1, i1, x)), s1[i1]);
    }

    public FingerTree<A> slice(int lo, int hi) {
        var b = new SliceBuilder<A>(lo, hi);
        b.consider(1, p1);
        b.consider(2, p2);
        b.consider(3, p3);
        b.consider(4, p4);
        b.consider(5, p5);
        b.consider(6, d6);
        b.consider(5, s5);
        b.consider(4, s4);
        b.consider(3, s3);
        b.consider(2, s2);
        b.consider(1, s1);
        return b.result();
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        if (s1.length > 1)
            return new FingerTreeAPI.Result<>(new Tree6<>(size - 1, len1, len12, len123, len1234, len12345, p1, p2, p3, p4, p5, d6, s5, s4, s3, s2, copyInit(s1)), s1[s1.length - 1]);
        else return new FingerTreeAPI.Result<>(slice(0, size - 1), s1[0]);
    }

    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        if (len1 > 1)
            return new FingerTreeAPI.Result<>(new Tree6<>(size - 1, len1 - 1, len12 - 1, len123 - 1, len1234 - 1, len12345 - 1,
                    copyTail(p1), p2, p3, p4, p5, d6, s5, s4, s3, s2, s1), p1[0]);
        else return new FingerTreeAPI.Result<>(slice(1, size), p1[0]);
    }

    @Override
    public int getSliceCount() {
        return 11;
    }

    public A[] getSliceAt(int idx) {
        //noinspection unchecked
        return (A[]) switch (idx) {
            case 0 -> p1;
            case 1 -> p2;
            case 2 -> p3;
            case 3 -> p4;
            case 4 -> p5;
            case 5 -> d6;
            case 6 -> s5;
            case 7 -> s4;
            case 8 -> s3;
            case 9 -> s2;
            case 10 -> s1;
            default -> throw new IllegalArgumentException("Unexpected value: " + idx);
        };
    }
}
