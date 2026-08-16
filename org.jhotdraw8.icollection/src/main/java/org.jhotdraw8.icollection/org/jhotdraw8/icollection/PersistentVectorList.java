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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
public abstract class PersistentVectorList<E> implements PersistentList<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    /// Constructs a new empty list.
    protected PersistentVectorList() {
    }

    @Override
    public PersistentVectorList<E> removingFirst() {
        return FingerTreeAPI.removeFirst(this).tree();
    }

    public PersistentVectorList<E> removingLast() {
        return FingerTreeAPI.removeLast(this).tree();
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentVectorList<T> of() {
        return FingerTreeAPI.of();
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
        return FingerTree.of();
    }

    @Override
    public PersistentVectorList<E> adding(E element) {
        return addingLast(element);
    }

    @Override
    public abstract PersistentVectorList<E> addingLast(@Nullable E element);

    @Override
    public abstract PersistentVectorList<E> addingFirst(@Nullable E element);

    @Override
    public PersistentVectorList<E> addingAt(int index, E element) {
        return FingerTreeAPI.addAt(this, index, element);
    }

    @Override
    public PersistentVectorList<E> addingAll(Iterable<? extends E> c) {
        return FingerTreeAPI.addAll(this, c);
    }

    @Override
    public PersistentVectorList<E> addingAllAt(int index, Iterable<? extends E> c) {
        return FingerTreeAPI.addAllAt(this, index, c);
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
        return index < 0 ? this : FingerTreeAPI.removeAt(this, index).tree();
    }

    @Override
    public PersistentVectorList<E> removingAt(int index) {
        return FingerTreeAPI.removeAt(this, index).tree();
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorList<E> retainingAll(Iterable<?> c) {
        return FingerTreeAPI.retainAll(this, c);
    }

    @Override
    public PersistentVectorList<E> removingRange(int fromIndex, int toIndex) {
        return FingerTreeAPI.removeRange(this, fromIndex, toIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentVectorList<E> removingAll(Iterable<?> c) {
        return FingerTreeAPI.removeAll(this, c);
    }


    @Override
    public PersistentVectorList<E> settingAt(int index, E element) {
        return FingerTreeAPI.setAt(this, index, element).tree();
    }

    @Override
    public PersistentVectorList<E> readableSubList(int fromIndex, int toIndex) {
        return FingerTreeAPI.slice(this, fromIndex, toIndex);
    }

    public int indexOf(Object o, int fromIndex) {
        if (fromIndex < size()) {
            for (Iterator<E> i = FingerTreeAPI.iterator(this, fromIndex, size()); i.hasNext(); fromIndex++) {
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
        return FingerTreeAPI.iterator(this);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
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
