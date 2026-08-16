/*
 * @(#)SimpleMutableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableListFacade;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedListView;
import org.jhotdraw8.icollection.serialization.ListSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

/// Implements the [List] interface using a bit-mapped trie (Vector).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///   - iterates in the order of the list
///
///
/// Performance characteristics:
///
///   - addLast: O(log N)
///   - set: O(log N)
///   - removeAt: O(N)
///   - removeFirst,removeLast: O(log N)
///   - contains: O(N)
///   - toPersistent: O(1)
///   - clone: O(1)
///   - iterator.next(): O(1)
///
///
/// References:
///
/// This class has been derived from Vavr Vector.java.
///
/// [vavr Vector.java](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/Vector.java)
/// [vavr MIT-License](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE)
///
/// @param <E> the element type
public class MutableVectorList<E> extends AbstractList<E> implements Serializable, ReadableList<E>, List<E>, Cloneable {
    @Serial
    private static final long serialVersionUID = 0L;

    @SuppressWarnings("TransientFieldNotInitialized")
    transient PersistentVectorList<E> tree;

    /// Constructs a new empty list.
    public MutableVectorList() {
        tree = FingerTreeAPI.of();
    }

    @Override
    public void addFirst(E e) {
        tree = FingerTreeAPI.addFirst(tree, e);
        modCount++;
    }

    @Override
    public void addLast(E e) {
        tree = FingerTreeAPI.addLast(tree, e);
        modCount++;
    }

    @Override
    public ReadableSequencedCollection<E> readableReversed() {
        return new ReadableListFacade<>(
                this::size,
                index -> get(tree.size() - 1 - index),
                () -> this
        );
    }

    @Override
    public List<E> reversed() {
        return new ReversedListView<>(this, this::modCount);
    }

    private int modCount() {
        return modCount;
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public E get(int index) {
        return tree.get(index);
    }

    @Override
    public E getFirst() {
        return tree.getFirst();
    }

    @Override
    public E getLast() {
        return tree.getLast();
    }

    @Override
    public ReadableList<E> readableSubList(int fromIndex, int toIndex) {
        return new ReadableListFacade<>(() -> toIndex - fromIndex, i -> get(i - fromIndex));
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        var newTree = tree.addingAll(c);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
        return false;
    }

    /// Adds all elements in the specified collection at the specified position.
    ///
    /// @param index the insertion position
    /// @param c     the collection to be added to ths list
    /// @return `true` if this list changed as a result of the call
    public boolean addAll(int index, Iterable<? extends E> c) {
        var newTree = tree.addingAllAt(index, c);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
        return false;
    }

    /// Adds all elements in the specified collection at the end of this list.
    ///
    /// @param c the collection to be added to ths list
    /// @return `true` if this list changed as a result of the call
    public boolean addAll(Iterable<? extends E> c) {
        var newTree = tree.addingAll(c);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        var newTree = tree.removingAll(c);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        var newTree = tree.retainingAll(c);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
        return false;
    }

    /// Constructs a list containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableVectorList(Iterable<? extends E> c) {
        this.tree = PersistentVectorList.copyOf(c);
    }

    public PersistentVectorList<E> toPersistent() {
        return tree;
    }

    @Serial
    private Object writeReplace() {
        return new MutableVectorList.SerializationProxy<>(this);
    }

    @Override
    public boolean add(E e) {
        tree = FingerTreeAPI.addLast(tree, e);
        modCount++;
        return true;

    }

    @Override
    public E set(int index, E element) {
        Objects.checkIndex(index, tree.size());
        E oldValue = get(index);
        tree = FingerTreeAPI.setAt(tree, index, element).tree();

        // According to Guava Tests, this method must not affect modCount!
        // modCount++;

        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        tree = FingerTreeAPI.addAt(tree, index, element);
        modCount++;
    }

    @Override
    public E remove(int index) {
        Objects.checkIndex(index, tree.size());
        E removed = get(index);
        removeRange(index, index + 1);
        return removed;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(FingerTreeAPI.iterator(tree), size(), Spliterator.ORDERED);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        var newTree = tree.removingRange(fromIndex, toIndex);
        if (newTree != tree) {
            tree = newTree;
            modCount++;
        }
    }

    @SuppressWarnings("FinalMethod")
    @Override
    public final MutableVectorList<E> clone() {
        try {
            return (MutableVectorList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
            return new MutableVectorList<>(deserializedElements);
        }
    }
}
