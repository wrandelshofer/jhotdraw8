/*
 * @(#)SimplePersistentList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableListFacade;
import org.jhotdraw8.icollection.impl.fingertree.FingerTree;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.serialization.ListSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;

/// Implements the [PersistentList] interface using a finger tree.
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///
/// Performance characteristics:
///
///   - addFirst, addLast: O(log₃₂ N)
///   - set: O(log₃₂ N)
///   - removeAt: O(N)
///   - removeFirst,removeLast: O(log₃₂ N)
///   - contains: O(N)
///   - toMutable: O(1)
///   - clone: O(1)
///   - iterator creation: O(1)
///   - iterator.next: O(1)
///   - getFirst, getLast: O(1)
///   - reversed: O(N)
///
/// References:
///
/// This code has been derived from
/// [Scala 3, Vector.scala](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/library/src/scala/collection/immutable/Vector.scala),
/// EPFL and Lightbend, Inc. dba Akka,
/// [Apache License 2.0](https://github.com/scala/scala3/blob/18df09c05fa48fbb5bf5cd4b3728e9b7a0b3f6db/LICENSE)
///
/// @param <E> the element type
public class PersistentVectorList<E> implements PersistentList<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final PersistentVectorList<?> EMPTY = new PersistentVectorList<>();
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient FingerTree<E> root;

    /// Constructs a new empty list.
    protected PersistentVectorList() {
        this.root = FingerTreeAPI.of();
    }

    /// Constructs a new list that contains all the elements of
    /// the specified iterable.
    ///
    /// @param iterable an iterable
    @SuppressWarnings("unchecked")
    protected PersistentVectorList(@Nullable Iterable<? extends E> iterable) {
        if (iterable == null) {
            this.root = FingerTreeAPI.of();
        } else if (iterable instanceof Collection<?> c && c.isEmpty()
                || iterable instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            this.root = FingerTreeAPI.of();
        } else if (iterable instanceof PersistentVectorList<? extends E> that) {
            this.root = (FingerTree<E>) that.root;
        } else if (iterable instanceof MutableVectorList<? extends E> mc) {
            PersistentVectorList<? extends E> that = mc.toPersistent();
            this.root = (FingerTree<E>) that.root;
        } else {
            this.root = FingerTreeAPI.copyOf(iterable);
        }
    }


    PersistentVectorList(FingerTree<E> trie) {
        this.root = trie;
    }

    /// Creates a new instance with the provided privateData data object.
    ///
    /// This constructor is intended to be called from a constructor
    /// of the subclass, that is called from method [#newInstance(PrivateData)].
    ///
    /// @param privateData an privateData data object
    protected PersistentVectorList(PrivateData privateData) {
        this.root = privateData.get();
    }

    /// Creates a new instance with the provided privateData object as its internal data structure.
    ///
    /// Subclasses must override this method, and return a new instance of their subclass!
    ///
    /// @param privateData the internal data structure needed by this class for creating the instance.
    /// @return a new instance of the subclass
    protected PersistentVectorList<E> newInstance(PrivateData privateData) {
        return new PersistentVectorList<>(privateData);
    }

    private PersistentVectorList<E> newInstance(FingerTree<E> trie) {
        return newInstance(new PrivateData(trie));
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentVectorList<T> of() {
        return (PersistentVectorList<T>) EMPTY;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> PersistentVectorList<T> of(T... t) {
        return new PersistentVectorListBuilder<T>().addArray(t).build();

    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentVectorList<T> copyOf(Iterable<? extends T> iterable) {
        return new PersistentVectorListBuilder<T>().addAll(iterable).build();
    }

    @Override
    public PersistentVectorList<E> cleared() {
        return of();
    }

    @Override
    public PersistentVectorList<E> adding(E element) {
        return newInstance(FingerTreeAPI.addLast(root, element));
    }


    @Override
    public PersistentVectorList<E> addingAt(int index, E element) {
        return newInstance(FingerTreeAPI.addAt(root, index, element));
    }

    @Override
    public PersistentVectorList<E> addingAll(Iterable<? extends E> c) {
        if (c instanceof PersistentVectorList<? extends E> p) {
            FingerTree<E> newRoot = FingerTreeAPI.addAll(root, p.root);
            return newRoot == root ? this : newInstance(newRoot);
        }
        FingerTree<E> newRoot = FingerTreeAPI.addAll(root, c);
        return newRoot == root ? this : newInstance(newRoot);
    }

    @Override
    public PersistentVectorList<E> addingFirst(@Nullable E element) {
        return newInstance(FingerTreeAPI.addFirst(element, root));
    }

    @Override
    public PersistentVectorList<E> addingLast(@Nullable E element) {
        return newInstance(FingerTreeAPI.addLast(root, element));
    }

    @Override
    public PersistentVectorList<E> addingAllAt(int index, Iterable<? extends E> c) {
        if (c instanceof PersistentVectorList<? extends E> p) {
            FingerTree<E> newRoot = FingerTreeAPI.addAllAt(root, index, p.root);
            return newRoot == root ? this : newInstance(newRoot);
        }
        FingerTree<E> newRoot = FingerTreeAPI.addAllAt(root, index, c);
        return newRoot == root ? this : newInstance(newRoot);
    }

    @Override
    public ReadableSequencedCollection<E> readableReversed() {
        return new ReadableListFacade<>(
                this::size,
                index -> get(size() - 1 - index),
                () -> this);
    }

    public PersistentVectorList<E> reversed() {
        return size() < 2 ? this : new PersistentVectorListBuilder<E>().addAll(readableReversed()).build();
    }

    @Override
    public PersistentVectorList<E> removing(E element) {
        int index = indexOf(element);
        return index < 0 ? this : removingAt(index);
    }

    @Override
    public PersistentVectorList<E> removingAt(int index) {
        return removingRange(index, index + 1);
    }

    @Override
    public PersistentVectorList<E> removingFirst() {
        return removingAt(0);
    }

    @Override
    public PersistentVectorList<E> removingLast() {
        return removingAt(size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorList<E> retainingAll(Iterable<?> c) {
        FingerTree<E> newRoot;
        if (c instanceof ReadableCollection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(root, e -> !cc.contains(e));
        } else if (c instanceof Collection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(root, e -> !cc.contains(e));
        } else {
            var set = new HashSet<E>();
            c.forEach(e -> set.add((E) e));
            newRoot = FingerTreeAPI.removeIf(root, e -> !set.contains(e));
        }
        return newRoot == root ? this : newInstance(newRoot);
    }

    @Override
    public PersistentVectorList<E> removingRange(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return this;
        return newInstance(FingerTreeAPI.removeRange(root, fromIndex, toIndex));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentVectorList<E> removingAll(Iterable<?> c) {
        FingerTree<E> newRoot;
        if (c instanceof ReadableCollection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(root, cc::contains);
        } else if (c instanceof Collection<?> cc) {
            newRoot = FingerTreeAPI.removeIf(root, cc::contains);
        } else {
            var set = new HashSet<E>();
            c.forEach(e -> set.add((E) e));
            newRoot = FingerTreeAPI.removeIf(root, set::contains);
        }
        return newRoot == root ? this : newInstance(newRoot);
    }


    @Override
    public PersistentVectorList<E> settingAt(int index, E element) {
        FingerTree<E> newRoot = FingerTreeAPI.setAt(root, index, element).tree();
        return newRoot == this.root ? this : newInstance(newRoot);
    }

    @Override
    public E get(int index) {
        return FingerTreeAPI.get(root, index);
    }

    @Override
    public PersistentVectorList<E> readableSubList(int fromIndex, int toIndex) {
        FingerTree<E> newRoot = FingerTreeAPI.slice(root, fromIndex, toIndex);
        return newRoot == this.root ? this : newInstance(newRoot);
    }

    @Override
    public int size() {
        return root.size();
    }

    public int indexOf(Object o, int fromIndex) {
        if (fromIndex < size()) {
            for (Iterator<E> i = FingerTreeAPI.iterator(root, fromIndex, size()); i.hasNext(); fromIndex++) {
                E e = i.next();
                if (Objects.equals(o, e)) {
                    return fromIndex;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        for (E e : this) {
            if (Objects.equals(e, o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ReadableList.iteratorToHashCode(iterator());
    }

    @Override
    public MutableVectorList<E> toMutable() {
        return new MutableVectorList<>(this);
    }

    @Serial
    private Object writeReplace() {
        return new PersistentVectorList.SerializationProxy<>(this.toMutable());
    }

    @Override
    public Iterator<E> iterator() {
        return FingerTreeAPI.iterator(root);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(FingerTreeAPI.iterator(root), size(), Spliterator.ORDERED);
    }

    @Override
    public boolean equals(Object obj) {
        return ReadableList.listEquals(this, obj);
    }

    /// Returns a string representation of this list.
    ///
    /// The string representation is consistent with the one produced
    /// by [AbstractList#toString()].
    ///
    /// @return a string representation
    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    private static class SerializationProxy<E> extends ListSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(List<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return new PersistentVectorListBuilder<>().addAll(deserializedElements);
        }
    }
}
