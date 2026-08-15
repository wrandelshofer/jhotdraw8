package org.jhotdraw8.icollection.impl.fingertree;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

/// This interface provides the API for a finger tree.
///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
public interface FingerTreeAPI {

    /// Adds the provided element at the specified offset to the node
    static <A> FingerTree<A> add(FingerTree<A> xs, int index, A x) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        if (index == 0) return xs.addFirst(x);
        if (index == xs.size() - 1) return xs.addLast(x);

        var a = xs.slice(0, index);
        var b = xs.slice(index + 1, xs.size());
        var builder = new FingerTreeBuilder<A>();
        builder.addVector(a);
        builder.addOne(x);
        builder.addVector(b);
        var c = builder.build();
        return c;
    }

    static <E> FingerTree<E> addAll(FingerTree<E> xs, Iterable<? extends E> ys) {
        var b = new FingerTreeBuilder<E>();
        b.addVector(xs);
        b.addAll(ys);
        var result = b.build();
        return result.size() == xs.size() ? xs : result;
    }

    static <E> @Nullable E getFirst(FingerTree<E> vector) {
        return vector.getFirst();
    }

    static <E> @Nullable E getLast(FingerTree<E> vector) {
        return vector.getLast();
    }

    static <E> FingerTree<E> removeIf(FingerTree<E> xs, Predicate<E> p) {
        var b = new FingerTreeBuilder<E>();
        for (var it = new FingerTreeIterator<>(xs); it.hasNext(); ) {
            var e = it.next();
            if (!p.test(e)) {
                b.addOne(e);
            }
        }
        var result = b.build();
        return result.size() == xs.size() ? xs : result;
    }

    @SuppressWarnings("unchecked")
    static <E> FingerTree<E> addAll(FingerTree<E> xs, FingerTree<? extends E> ys) {
        if (xs.size() + ys.size() < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", ys.size=" + ys.size());
        if (ys.size() == 0) return xs;
        if (xs.size() == 0) return (FingerTree<E>) ys;
        var b = new FingerTreeBuilder<E>();
        b.addVector(xs);
        b.addVector(ys);
        return b.build();
    }

    static <E> FingerTree<E> addAllAt(FingerTree<E> xs, int index, Iterable<? extends E> ys) {
        if (index == xs.size() - 1) return addAll(xs, ys);
        var b = new FingerTreeBuilder<E>();
        b.addVector(xs);
        b.addAll(ys);
        var result = b.build();
        return result.size() == xs.size() ? xs : result;
    }

    static <E> FingerTree<E> addAllAt(FingerTree<E> xs, int index, FingerTree<? extends E> ys) {
        if (index == xs.size() - 1) return addAll(xs, ys);
        if (xs.size() + ys.size() < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", ys.size=" + ys.size());
        if (ys.size() == 0) return xs;
        var b = new FingerTreeBuilder<E>();
        b.addVector(xs);
        b.addVector(ys);
        return b.build();
    }

    /// Prepends the provided element to the node
    static <A> FingerTree<A> addFirst(@Nullable A x, FingerTree<A> xs) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        return xs.addFirst(x);
    }

    /// Appends the provided element to the node
    static <A> FingerTree<A> addLast(FingerTree<A> xs, @Nullable A x) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        return xs.addLast(x);
    }

    /// Appends the provided element to the node
    static <A> FingerTree<A> addAt(FingerTree<A> xs, int index, A x) {
        Objects.checkIndex(index, xs.size() + 1);
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        if (index == 0) return xs.addFirst(x);
        if (index == xs.size()) return xs.addLast(x);
        var b = new FingerTreeBuilder<A>();
        b.addVector(xs.slice(0, index));
        b.addOne(x);
        b.addVector(xs.slice(index, xs.size()));
        return b.build();
    }

    /**
     * Creates a Tree with the same element at each offset.
     * <p>
     * Unlike `fill`, which takes a by-name argument for the value and can thereby
     * compute different values for each offset, this method guarantees that all
     * elements are identical. This allows sparse allocation in O(log n) time and space.
     *
     * @param n    the number of elements in the vector
     * @param elem the element to fill every position with
     * @return a new vector of size `n` with each element set to `elem`
     * @tparam A the element type of the vector
     */
    static <A> FingerTree<A> fillSparse(int n, A elem) {
        if (n <= 0) return of();
        else {
            var b = new FingerTreeBuilder<A>();
            b.initSparse(n, elem);
            return b.build();
        }
    }

    /// Gets the element at the specified offset
    static <A> @Nullable A get(FingerTree<A> xs, int index) {
        Objects.checkIndex(index, xs.size());
        return xs.get(index);
    }

    /// Returns the root element of an empty finger tree
    static <A> FingerTree<A> of() {
        //noinspection unchecked
        return (FingerTree<A>) Tree0.INSTANCE;
    }

    /// Returns a finger tree that contains all elements of the specified iterable
    static <A> FingerTree<A> copyOf(Iterable<? extends A> iterable) {
        var b = new FingerTreeBuilder<A>();
        b.addAll(iterable);
        return b.build();
    }

    /// Removes the element at the specified offset
    static <A> Result<A> removeAt(FingerTree<A> xs, int index) {
        return xs.removeAt(index);
    }

    /// Removes the elements in the specified range
    static <A> FingerTree<A> removeRange(FingerTree<A> xs, int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return xs;
        if (fromIndex == 0) return xs.slice(toIndex, xs.size());
        if (toIndex == xs.size()) return xs.slice(0, fromIndex);
        var b = new FingerTreeBuilder<A>();
        b.addVector(xs.slice(0, fromIndex));
        b.addVector(xs.slice(toIndex, xs.size()));
        return b.build();
    }

    /// Retains the elements in the specified range
    static <A> FingerTree<A> slice(FingerTree<A> xs, int fromIndex, int toIndex) {
        return xs.slice(fromIndex, toIndex);
    }

    static <A> Iterator<A> iterator(FingerTree<A> xs) {
        return new FingerTreeIterator<>(xs);
    }

    static <A> Iterator<A> iterator(FingerTree<A> xs, int fromIndex, int toIndex) {
        return new FingerTreeIterator<>(xs, fromIndex, toIndex);
    }

    /// Removes the tree element, returns the updated node and the removed element
    static <A> Result<A> removeFirst(FingerTree<A> xs) {
        return xs.removeFirst();
    }

    /// Removes the last element, returns the updated node and the removed element
    static <A> Result<A> removeLast(FingerTree<A> xs) {
        return xs.removeLast();
    }

    /// Sets the element at the specified offset
    static <A> Result<A> setAt(FingerTree<A> xs, int index, A element) {
        Result<A> rr = xs.set(index, element);
        return Objects.equals(rr.element, element) ? new Result<>(xs, rr.element) : rr;
    }

    record Result<A>(FingerTree<A> tree, @Nullable A element) {
    }
}
