package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Objects;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyAppend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyInit;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyUpdate;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty3;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty5;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap4;

///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public final class Tree4<A> extends FingerTree<A> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final int size;
    private final byte len1;
    private final short len12;
    private final char len123;
    private final A[] p1;
    private final A[][] p2;
    private final A[][][] p3;
    private final A[][][][] d4;
    private final A[][][] s3;
    private final A[][] s2;
    private final A[] s1;

    /**
     *
     */
    public Tree4(int size, byte len1, short len12, char len123, A[] p1, A[][] p2, A[][][] p3, A[][][][] d4,
                 A[][][] s3, A[][] s2, A[] s1) {
        this.size = size;
        this.len1 = len1;
        this.len12 = len12;
        this.len123 = len123;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.d4 = d4;
        this.s3 = s3;
        this.s2 = s2;
        this.s1 = s1;
    }

    public Tree4(int size, A[] p1, A[][] p2, A[][][] p3, A[][][][] d4, A[][][] s3, A[][] s2, A[] s1) {
        var len1 = p1.length;
        var len12 = len1 + p2.length * WIDTH;
        var len123 = len12 + p3.length * WIDTH2;
        this(size, (byte) len1, (short) len12, (char) len123, p1, p2, p3, d4, s3, s2, s1);
    }

    public Tree4(int size, int len1, int len12, int len123, A[] p1, A[][] p2, A[][][] p3, A[][][][] d4, A[][][] s3, A[][] s2, A[] s1) {
        this(size, (byte) len1, (short) len12, (char) len123, p1, p2, p3, d4, s3, s2, s1);
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
            return new Tree4<>(size + 1, len1, len12, len123,
                    p1, p2, p3, d4, s3, s2, copyAppend(s1, x));
        if (s2.length < WIDTH - 1)
            return new Tree4<>(size + 1, len1, len12, len123,
                    p1, p2, p3, d4, s3, copyAppend(s2, s1), wrap1(x));
        if (s3.length < WIDTH - 1)
            return new Tree4<>(size + 1, len1, len12, len123,
                    p1, p2, p3, d4, copyAppend(s3, copyAppend(s2, s1)), empty2(), wrap1(x));
        if (d4.length < WIDTH - 2)
            return new Tree4<>(size + 1, len1, len12, len123,
                    p1, p2, p3, copyAppend(d4, copyAppend(s3, copyAppend(s2, s1))), empty3(), empty2(), wrap1(x));

        return new Tree5<>(size + 1, len1, len12, len123, (WIDTH - 2) * WIDTH3 + len123,
                p1, p2, p3, d4, empty5(), wrap4(copyAppend(s3, copyAppend(s2, s1))), empty3(), empty2(), wrap1(x));

    }

    @Override
    public int getSliceCount() {
        return 7;
    }

    public A[] getSliceAt(int idx) {
        //noinspection unchecked
        return (A[]) switch (idx) {
            case 0 -> p1;
            case 1 -> p2;
            case 2 -> p3;
            case 3 -> d4;
            case 4 -> s3;
            case 5 -> s2;
            case 6 -> s1;
            default -> throw new IllegalArgumentException("Unexpected value: " + idx);
        };
    }

    @Override
    public FingerTree<A> addingFirst(A x) {
        if (len1 < WIDTH)
            return new Tree4<>(size + 1, len1 + 1, len12 + 1, len123 + 1,
                    copyPrepend(x, p1), p2, p3, d4, s3, s2, s1);
        if (len12 < WIDTH2)
            return new Tree4<>(size + 1, 1, len12 + 1, len123 + 1,
                    wrap1(x), copyPrepend(p1, p2), p3, d4, s3, s2, s1);
        if (len123 < WIDTH3)
            return new Tree4<>(size + 1, 1, 1, len123 + 1,
                    wrap1(x), empty2(), copyPrepend(copyPrepend(p1, p2), p3), d4, s3, s2, s1);
        if (d4.length < WIDTH - 2)
            return new Tree4<>(size + 1, 1, 1, 1,
                    wrap1(x), empty2(), empty3(), copyPrepend(copyPrepend(copyPrepend(p1, p2), p3), d4), s3, s2, s1);

        return new Tree5<>(size + 1, 1, 1, 1, len123 + 1,
                wrap1(x), empty2(), empty3(), wrap4(copyPrepend(copyPrepend(p1, p2), p3)), empty5(), d4, s3, s2, s1);

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
        var io = index - len123;
        var i4 = io >>> BITS3;
        var i3 = (io >>> BITS2) & MASK;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i4 < d4.length) return d4[i4][i3][i2][i1];
        if (i3 < s3.length) return s3[i3][i2][i1];
        if (i2 < s2.length) return s2[i2][i1];
        return s1[i1];
    }

    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        if (len1 > index)
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, copyUpdate(p1, index, x), p2, p3, d4, s3, s2, s1), p1[index]);
        if (len12 > index) {
            var io = index - len1;
            int i2 = io >>> BITS;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, copyUpdate(p2, i2, i1, x), p3, d4, s3, s2, s1), p2[i2][i1]);
        }
        if (len123 > index) {
            var io = index - len12;
            int i3 = io >>> BITS2;
            int i2 = (io >>> BITS) & MASK;
            int i1 = io & MASK;
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, p2, copyUpdate(p3, i3, i2, i1, x), d4, s3, s2, s1), p3[i3][i2][i1]);
        }
        var io = index - len123;
        var i4 = io >>> BITS3;
        var i3 = (io >>> BITS2) & MASK;
        var i2 = (io >>> BITS) & MASK;
        var i1 = io & MASK;
        if (i4 < d4.length)
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, p2, p3, copyUpdate(d4, i4, i3, i2, i1, x), s3, s2, s1), d4[i4][i3][i2][i1]);
        if (i3 < s3.length)
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, p2, p3, d4, copyUpdate(s3, i3, i2, i1, x), s2, s1), s3[i3][i2][i1]);
        if (i2 < s2.length)
            return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, p2, p3, d4, s3, copyUpdate(s2, i2, i1, x), s1), s2[i2][i1]);
        return new FingerTreeAPI.Result<>(new Tree4<>(size, len1, len12, len123, p1, p2, p3, d4, s3, s2, copyUpdate(s1, i1, x)), s1[i1]);
    }

    @Override
    public FingerTree<A> slice(int lo, int hi) {
        var b = new SliceBuilder<A>(lo, hi);
        b.consider(1, p1);
        b.consider(2, p2);
        b.consider(3, p3);
        b.consider(4, d4);
        b.consider(3, s3);
        b.consider(2, s2);
        b.consider(1, s1);
        return b.result();
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        if (s1.length > 1)
            return new FingerTreeAPI.Result<>(new Tree4<>(size - 1, len1, len12, len123, p1, p2, p3, d4, s3, s2, copyInit(s1)), s1[s1.length - 1]);
        else return new FingerTreeAPI.Result<>(slice(0, size - 1), s1[0]);
    }


    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        if (len1 > 1)
            return new FingerTreeAPI.Result<>(new Tree4<>(size - 1, len1 - 1, len12 - 1, len123 - 1, copyTail(p1), p2, p3, d4, s3, s2, s1), p1[0]);
        else return new FingerTreeAPI.Result<>(slice(1, size), p1[0]);
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

    char len123() {
        return len123;
    }

    A[] p1() {
        return p1;
    }

    A[][] p2() {
        return p2;
    }

    A[][][] p3() {
        return p3;
    }

    A[][][][] d4() {
        return d4;
    }

    A[][][] s3() {
        return s3;
    }

    A[][] s2() {
        return s2;
    }

    A[] s1() {
        return s1;
    }

    public int _indexOf(Object o, int fromIndex, int toIndex) {
        int i = fromIndex;
        for (; i < Math.min(toIndex, len1); i++) {
            if (Objects.equals(p1[i], o)) return i;
        }
        for (; i < Math.min(toIndex, len12); i++) {
            int index = i - len1;
            if (Objects.equals(p2[index >> BITS][index & MASK], o)) return i;
        }
        for (; i < Math.min(toIndex, len123); i++) {
            int index = i - len12;
            if (Objects.equals(p3[index >> BITS2][(index >> BITS) & MASK][index & MASK], o)) return i;
        }
        for (; i < Math.min(toIndex, len123 + d4.length * WIDTH3); i++) {
            int index = i - len123;
            if (Objects.equals(d4[index >> BITS3][(index >> BITS2) & MASK][(index >> BITS) & MASK][index & MASK], o))
                return i;
        }
        for (; i < Math.min(toIndex, size - s1.length - s2.length * WIDTH); i++) {
            int index = i - len123 - d4.length * WIDTH3;
            if (Objects.equals(s3[index >> BITS2][(index >> BITS) & MASK][index & MASK], o)) return i;
        }
        for (; i < Math.min(toIndex, size - s1.length); i++) {
            int index = i - len123 - d4.length * WIDTH3 - s3.length * WIDTH2;
            if (Objects.equals(s2[index >> BITS][index & MASK], o)) return i;
        }
        for (; i < Math.min(toIndex, size); i++) {
            int index = i - size + s1.length;
            if (Objects.equals(s1[index], o)) return i;
        }
        return -1;
    }
}
