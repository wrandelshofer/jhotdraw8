package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import java.io.Serial;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyAppend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyInit;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyPrepend;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyRemove;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copySlice;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyTail;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.copyUpdate;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.empty2;
import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;

///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public final class Tree1<A> extends FingerTree<A> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final A[] d1;

    /**
     *
     */
    public Tree1(A[] d1) {
        this.d1 = d1;
    }

    @Override
    public int size() {
        return d1.length;
    }

    @Override
    public int getSliceCount() {
        return 1;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public @Nullable A getFirst() {
        return d1[0];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @Nullable A getLast() {
        return d1[d1.length - 1];
    }

    public A[] getSliceAt(int idx) {
        //noinspection unchecked
        return d1;
    }

    @Override
    public FingerTree<A> slice(int from, int to) {
        return new Tree1<>(copySlice(d1, from, to));
    }


    @Override
    public FingerTree<A> addingLast(A x) {
        if (d1.length < WIDTH)
            return new Tree1<>(copyAppend(d1, x));

        return new Tree2<>(WIDTH + 1, WIDTH, d1, empty2(), wrap1(x));
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        if (d1.length == 1)
            return new FingerTreeAPI.Result<>(FingerTreeAPI.of(), d1[0]);
        return new FingerTreeAPI.Result<>(new Tree1<>(copyInit(d1)), d1[d1.length - 1]);
    }

    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        if (d1.length == 1)
            return new FingerTreeAPI.Result<>(FingerTreeAPI.of(), d1[0]);
        return new FingerTreeAPI.Result<>(new Tree1<>(copyTail(d1)), d1[0]);
    }

    @Override
    public FingerTree<A> addingFirst(A x) {
        if (d1.length < WIDTH)
            return new Tree1<>(copyPrepend(x, d1));

        return new Tree2<>(WIDTH + 1, 1, wrap1(x), empty2(), d1);
    }

    @Override
    public @Nullable A get(int index) {
        return d1[index];
    }

    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        return new FingerTreeAPI.Result<>(new Tree1<>(copyUpdate(d1, index, x)), d1[index]);
    }

    @Override
    public FingerTreeAPI.Result<A> removeAt(int index) {
        return new FingerTreeAPI.Result<>(new Tree1<>(copyRemove(d1, index)), d1[index]);
    }

    A[] d1() {
        return d1;
    }
}
