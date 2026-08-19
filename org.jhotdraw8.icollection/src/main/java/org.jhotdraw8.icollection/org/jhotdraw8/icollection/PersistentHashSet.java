/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.champset.DeltaCounter;
import org.jhotdraw8.icollection.impl.champset.ElementIterator;
import org.jhotdraw8.icollection.impl.champset.TrieBuilder;
import org.jhotdraw8.icollection.impl.champset.TrieNode;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;


/// Implements the [PersistentSet] interface using a Compressed Hash-Array
/// Mapped Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N)
///   - remove: O(log₃₂ N
///   - contains: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy
///   - clone: O(1)
///   - iterator.next(): O(1)
///
///
/// Implementation details:
///
/// This set performs read and write operations of single elements in O(log₃₂ N) time,
/// and in O(log₃₂ N) space.
///
/// The CHAMP trie contains nodes that may be shared with other sets.
///
/// If a write operation is performed on a node, then this set creates a
/// copy of the node and of all parent nodes up to the root (copy-path-on-write).
///
/// This set can create a mutable copy of itself in O(1) time and O(1) space
/// using method [#toMutable()]. The mutable copy shares its nodes
/// with this set, until it has gradually replaced the nodes with exclusively
/// owned nodes.
///
/// All operations on this set can be performed concurrently, without a need for
/// synchronisation.
///
/// References:
///
/// Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>Michael J. Steindorfer (2017).
///      Efficient Persistent Collections.</dt>
///      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a></dd>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a></dd>
/// </dl>
///
/// @param <E> the element type
@SuppressWarnings("exports")
public class PersistentHashSet<E> implements PersistentSet<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final PersistentHashSet<?> EMPTY = new PersistentHashSet<Object>(TrieNode.EMPTY, 0);
    final TrieNode<E> node;
    final int size;

    @SuppressWarnings("unchecked")
    public static <T> PersistentHashSet<T> of() {
        return (PersistentHashSet<T>) EMPTY;
    }

    public static <T> PersistentHashSet<T> of(T... elements) {
        return new PersistentHashSetBuilder<T>().addArray(elements).build();
    }

    public static <T> PersistentHashSet<T> copyOf(Iterable<T> elements) {
        return new PersistentHashSetBuilder<T>().addAll(elements).build();
    }

    public static <T> PersistentHashSetBuilder<T> builder() {
        return new PersistentHashSetBuilder<T>();
    }

    PersistentHashSet(TrieNode<E> node, int size) {
        this.node = node;
        this.size = size;
    }

    @Override
    public PersistentHashSet<E> cleared() {
        return of();
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public PersistentHashSet<E> adding(E element) {
        var newNode = node.add(Objects.hashCode(element), element, 0);
        if (newNode == node) {
            return this;
        }
        return new PersistentHashSet<>(newNode, size + 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashSet<E> addingAll(Iterable<? extends E> c) {
        if (c instanceof MutableHashSet<? extends E> m) {
            c = m.toPersistent();
        }
        if (c instanceof PersistentHashSet<? extends E> m) {
            var deltaCounter = new DeltaCounter();
            var builder = new TrieBuilder<>();
            var newNode = node.mutableAddAll((TrieNode<E>) m.node, 0, deltaCounter, builder);
            var newSize = size + m.size - deltaCounter.count;
            return (size != newSize) ? new PersistentHashSet<>(newNode, newSize) : this;
        }
        return (PersistentHashSet<E>) PersistentSet.super.addingAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashSet<E> removingAll(Iterable<?> c) {
        if (c instanceof MutableHashSet<?> m) {
            c = m.toPersistent();
        }
        if (c instanceof PersistentHashSet<?> m) {
            var deltaCounter = new DeltaCounter();
            var builder = new TrieBuilder<>();
            TrieNode<E> newNode = (TrieNode<E>) node.mutableRemoveAll((TrieNode<E>) m.node, 0, deltaCounter, builder);
            var newSize = size - deltaCounter.count;
            return (newSize != size) ? new PersistentHashSet<>(newNode, newSize) : this;
        }
        return (PersistentHashSet<E>) PersistentSet.super.removingAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashSet<E> retainingAll(Iterable<?> c) {
        if (c instanceof MutableHashSet<?> m) {
            c = m.toPersistent();
        }
        if (c instanceof PersistentHashSet<?> m) {
            var deltaCounter = new DeltaCounter();
            var builder = new TrieBuilder<>();
            TrieNode<E> newNode = (TrieNode<E>) node.mutableRetainAll((TrieNode<E>) m.node, 0, deltaCounter, builder);
            var newSize = deltaCounter.count;
            return (newSize != size) ? new PersistentHashSet<>(newNode, newSize) : this;
        }
        return (PersistentHashSet<E>) PersistentSet.super.retainingAll(c);
    }

    @Override
    public PersistentHashSet<E> removing(E element) {
        var newNode = node.remove(Objects.hashCode(element), element, 0);
        if (newNode == node) {
            return this;
        }
        return new PersistentHashSet<>(newNode, size - 1);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        return node.contains(Objects.hashCode(o), (E) o, 0);
    }

    @Override
    public Iterator<E> iterator() {
        return new ElementIterator<>(node);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.IMMUTABLE);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return PersistentHashSet.builder().addAll(deserializedElements).build();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return ReadableSet.setEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(this.iterator());
    }

    public MutableHashSet<E> toMutable() {
        return new MutableHashSet<>(this.node, size);
    }

    public MutableHashSet<E> asSet() {
        return new MutableHashSet<>(this.node, size);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }
}