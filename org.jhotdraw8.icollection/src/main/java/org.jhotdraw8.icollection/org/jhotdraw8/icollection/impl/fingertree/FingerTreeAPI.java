package org.jhotdraw8.icollection.impl.fingertree;

import org.jhotdraw8.icollection.PersistentVectorList;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
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
    static <A> PersistentVectorList<A> add(FingerTree<A> xs, int index, A x) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        if (index == 0) return xs.addingFirst(x);
        if (index == xs.size() - 1) return xs.addingLast(x);

        var a = xs.slice(0, index);
        var b = xs.slice(index + 1, xs.size());
        var builder = new FingerTreeBuilder<A>();
        builder.addVector(a);
        builder.addOne(x);
        builder.addVector(b);
        var c = builder.build();
        return c;
    }

    static <E> PersistentVectorList<E> addAll(PersistentVectorList<E> list, Iterable<? extends E> ys) {
        var b = new FingerTreeBuilder<E>();
        b.addVector((FingerTree<? extends E>) list);
        if (ys instanceof FingerTree<? extends E> p) {
            b.addVector(p);
        } else {
            b.addAll(ys);
        }
        var result = b.build();
        return result.size() == list.size() ? list : result;
    }

    @SuppressWarnings("unchecked")
    static <E> PersistentVectorList<E> retainAll(PersistentVectorList<E> list, Iterable<?> c) {
        FingerTree<E> newRoot;
        if (c instanceof ReadableCollection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(list, e -> !cc.contains(e));
        } else if (c instanceof Collection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(list, e -> !cc.contains(e));
        } else {
            var set = new HashSet<E>();
            c.forEach(e -> set.add((E) e));
            newRoot = FingerTreeAPI.removeIf(list, e -> !set.contains(e));
        }
        return newRoot;
    }

    @SuppressWarnings("unchecked")
    static <E> PersistentVectorList<E> removeAll(PersistentVectorList<E> list, Iterable<?> c) {
        FingerTree<E> newRoot;
        if (c instanceof ReadableCollection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(list, cc::contains);
        } else if (c instanceof Collection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(list, cc::contains);
        } else {
            var set = new HashSet<E>();
            c.forEach(e -> set.add((E) e));
            newRoot = FingerTreeAPI.removeIf(list, set::contains);
        }
        return newRoot;
    }

    static <E> E getFirst(PersistentVectorList<E> vector) {
        return vector.getFirst();
    }

    static <E> E getLast(PersistentVectorList<E> vector) {
        return vector.getLast();
    }

    static <E> FingerTree<E> removeIf(PersistentVectorList<E> list, Predicate<E> p) {
        FingerTree<E> xs = (FingerTree<E>) list;
        var b = new FingerTreeBuilder<E>();
        for (var it = FingerTreeAPI.iterator(xs); it.hasNext(); ) {
            var e = it.next();
            if (!p.test(e)) {
                b.addOne(e);
            }
        }
        var result = b.build();
        return result.size() == xs.size() ? xs : result;
    }

    @SuppressWarnings({"unchecked", "SizeReplaceableByIsEmpty"})
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

    static <E> PersistentVectorList<E> addAllAt(PersistentVectorList<E> list, int index, Iterable<? extends E> ys) {
        FingerTree<E> xs = (FingerTree<E>) list;
        if (index == xs.size() - 1) return addAll(xs, ys);
        var b = new FingerTreeBuilder<E>();
        b.addVector(xs);
        if (ys instanceof FingerTree<? extends E> p) {
            b.addVector(p);
        } else {
            b.addAll(ys);
        }
        var result = b.build();
        return result.size() == xs.size() ? xs : result;
    }

    /// Prepends the provided element to the node
    static <A> PersistentVectorList<A> addFirst(PersistentVectorList<A> xs, @Nullable A x) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        return xs.addingFirst(x);
    }

    /// Appends the provided element to the node
    static <A> PersistentVectorList<A> addLast(PersistentVectorList<A> xs, @Nullable A x) {
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        return xs.addingLast(x);
    }

    /// Appends the provided element to the node
    static <A> PersistentVectorList<A> addAt(PersistentVectorList<A> list, int index, A x) {
        FingerTree<A> xs = (FingerTree<A>) list;
        Objects.checkIndex(index, xs.size() + 1);
        if (xs.size() + 1 < 0)
            throw new IllegalArgumentException("Combined size exceeds maximal capacity. tree.size=" + xs.size() + ", element.size=1");
        if (index == 0) return xs.addingFirst(x);
        if (index == xs.size()) return xs.addingLast(x);
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
    static <A> A get(PersistentVectorList<A> xs, int index) {
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
    static <A> Result<A> removeAt(PersistentVectorList<A> list, int index) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return xs.removeAt(index);
    }

    /// Removes the elements in the specified range
    static <A> FingerTree<A> removeRange(PersistentVectorList<A> list, int fromIndex, int toIndex) {
        FingerTree<A> xs = (FingerTree<A>) list;
        if (fromIndex == toIndex) return xs;
        if (fromIndex == 0) return xs.slice(toIndex, xs.size());
        if (toIndex == xs.size()) return xs.slice(0, fromIndex);
        var b = new FingerTreeBuilder<A>();
        b.addVector(xs.slice(0, fromIndex));
        b.addVector(xs.slice(toIndex, xs.size()));
        return b.build();
    }

    /// Retains the elements in the specified range
    static <A> FingerTree<A> slice(PersistentVectorList<A> list, int fromIndex, int toIndex) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return xs.slice(fromIndex, toIndex);
    }


    static <A> FingerTreeIterator<A> iterator(PersistentVectorList<A> list) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return new FingerTreeIterator<>(xs);
    }

    static <A> Iterator<A> iterator(PersistentVectorList<A> list, int fromIndex, int toIndex) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return new FingerTreeIterator<>(xs, fromIndex, toIndex);
    }

    /// Removes the tree element, returns the updated node and the removed element
    static <A> Result<A> removeFirst(PersistentVectorList<A> list) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return xs.removeFirst();
    }

    /// Removes the last element, returns the updated node and the removed element
    static <A> Result<A> removeLast(PersistentVectorList<A> list) {
        FingerTree<A> xs = (FingerTree<A>) list;
        return xs.removeLast();
    }

    /// Sets the element at the specified offset
    static <A> Result<A> setAt(PersistentVectorList<A> list, int index, A element) {
        FingerTree<A> xs = (FingerTree<A>) list;
        Result<A> rr = xs.set(index, element);
        return Objects.equals(rr.element, element) ? new Result<>(xs, rr.element) : rr;
    }

    record Result<A>(PersistentVectorList<A> tree, @Nullable A element) {
    }
}
