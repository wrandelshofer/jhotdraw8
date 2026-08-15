package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyAppend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyInit;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyUpdate;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty3;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap2;

///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public record Tree2<A>(int size, byte len1, A[] p1, A[][] d2, A[] s1) implements FingerTree<A> {
    public Tree2(int size, int len1, A[] p1, A[][] d2, A[] s1) {
        this(size, (byte) len1, p1, d2, s1);
    }

    public Tree2(int size, A[] p1, A[][] d2, A[] s1) {
        this(size, (byte) p1.length, p1, d2, s1);
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
    public FingerTree<A> addFirst(A x) {
        if (len1 < WIDTH)
            return new Tree2<>(size + 1, len1 + 1,//
                    copyPrepend(x, p1), d2, s1);
        if (d2().length < WIDTH - 2)
            return new Tree2<>(size + 1, 1,//
                    wrap1(x), copyPrepend(p1, d2), s1);

        return new Tree3<>(size + 1, 1, len1 + 1,
                wrap1(x), wrap2(p1), empty3(), d2, s1);
    }

    @Override
    public FingerTree<A> addLast(A x) {
        if (s1().length < WIDTH)
            return new Tree2<>(size + 1, len1, p1, d2, copyAppend(s1, x));
        if (d2().length < WIDTH - 2)
            return new Tree2<>(size + 1, len1, p1, copyAppend(d2, s1), wrap1(x));

        return new Tree3<>(size + 1, len1, WIDTH * (WIDTH - 2) + len1, p1, d2, empty3(), wrap2(s1), wrap1(x));
    }

    @Override
    public @Nullable A get(int index) {
        if (len1 > index) return p1[index];
        var io = index - len1;
        var i2 = io >>> BITS;
        var i1 = io & MASK;
        return (i2 < d2.length) ? d2[i2][i1] : s1[i1];
    }

    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        if (len1 > 1)
            return new FingerTreeAPI.Result<>(new Tree2<>(size - 1, len1 - 1, copyTail(p1), d2, s1), p1[0]);
        return new FingerTreeAPI.Result<>(slice(1, size), p1[0]);
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        if (s1().length > 1)
            return new FingerTreeAPI.Result<>(new Tree2<>(size - 1, len1, p1, d2, copyInit(s1)), s1[s1.length - 1]);
        return new FingerTreeAPI.Result<>(slice(0, size - 1), s1[0]);
    }

    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        if (len1 > index) {
            return new FingerTreeAPI.Result<>(new Tree2<>(size, len1, copyUpdate(p1, index, x), d2, s1), p1[index]);
        }
        var io = index - len1;
        var i2 = io >>> BITS;
        var i1 = io & MASK;
        if (i2 < d2.length) {
            return new FingerTreeAPI.Result<>(new Tree2<>(size, len1, p1, copyUpdate(d2, i2, i1, x), s1), d2[i2][i1]);
        }
        return new FingerTreeAPI.Result<>(new Tree2<>(size, len1, p1, d2, copyUpdate(s1, i1, x)), s1[i1]);
    }

    @Override
    public FingerTree<A> slice(int lo, int hi) {
        var b = new SliceBuilder<A>(lo, hi);
        b.consider(1, p1);
        b.consider(2, d2);
        b.consider(1, s1);
        return b.result();
    }

    public A[] getSliceAt(int idx) {
        //noinspection unchecked
        return (A[]) switch (idx) {
            case 0 -> p1;
            case 1 -> d2;
            case 2 -> s1;
            default -> throw new IllegalArgumentException("Unexpected value: " + idx);
        };
    }

    @Override
    public int getSliceCount() {
        return 3;
    }


}
