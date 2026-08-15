package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;

import static org.jhotdraw8.icollection.impl.fingertree.Arr.wrap1;

///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public record Tree0<A>() implements FingerTree<A> {
    public final static Tree0<?> INSTANCE = new Tree0<>();

    /// Returns the root element of an empty Fingertree
    public static <A> Tree0<A> empty() {
        //noinspection unchecked
        return (Tree0<A>) Tree0.INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int getSliceCount() {
        return 0;
    }

    public A[] getSliceAt(int idx) {
        throw new ArrayIndexOutOfBoundsException(idx);
    }

    @Override
    public FingerTree<A> slice(int from, int to) {
        return this;
    }

    @Override
    public FingerTree<A> addLast(A x) {
        return new Tree1<>(wrap1(x));
    }

    @Override
    public FingerTree<A> addFirst(A x) {
        return new Tree1<>(wrap1(x));
    }

    @Override
    public @Nullable A get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public @Nullable A getFirst() {
        throw new NoSuchElementException();
    }

    @Override
    public @Nullable A getLast() {
        throw new NoSuchElementException();
    }

    @Override
    public FingerTreeAPI.Result<A> set(int index, A x) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public FingerTreeAPI.Result<A> removeLast() {
        throw new NoSuchElementException();
    }

    @Override
    public FingerTreeAPI.Result<A> removeFirst() {
        throw new NoSuchElementException();
    }
}
