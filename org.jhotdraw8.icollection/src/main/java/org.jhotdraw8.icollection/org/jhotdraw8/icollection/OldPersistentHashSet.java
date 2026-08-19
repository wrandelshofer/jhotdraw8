/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChampIterator;
import org.jhotdraw8.icollection.alt.impl.champset.ChampSpliterator;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.alt.impl.champset.Node;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;


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
public class OldPersistentHashSet<E> implements PersistentSet<E>, Serializable {
    /// We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
    static final int SALT = 0;// new Random().nextInt();
    private static final OldPersistentHashSet<?> EMPTY = new OldPersistentHashSet<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode<E> root;
    final int size;

    OldPersistentHashSet(BitmapIndexedNode<E> root, int size) {
        this.root = root;
        this.size = size;
    }


    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param c   an iterable
    /// @param <E> the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings("unchecked")
    public static <E> OldPersistentHashSet<E> copyOf(Iterable<? extends E> c) {
        return OldPersistentHashSet.<E>of().addingAll(c);
    }

    /// Returns an empty persistent set.
    ///
    /// @param <E> the element type
    /// @return an empty persistent set
    @SuppressWarnings("unchecked")
    public static <E> OldPersistentHashSet<E> of() {
        return ((OldPersistentHashSet<E>) OldPersistentHashSet.EMPTY);
    }

    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param elements elements
    /// @param <E>      the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> OldPersistentHashSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new OldPersistentHashSetBuilder<E>().addArray(elements).build();
    }

    /// Update function for a set: we always keep the old element.
    ///
    /// @param oldElement the old element
    /// @param newElement the new element
    /// @param <E>        the element type
    /// @return always returns the old element
    static <E> E keepOldElement(E oldElement, E newElement) {
        return oldElement;
    }

    static int keyHash(@Nullable Object e) {
        return SALT ^ Objects.hashCode(e);
    }

    @Override
    public OldPersistentHashSet<E> adding(@Nullable E element) {
        int keyHash = keyHash(element);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.put(null, element, keyHash, 0, details,
                OldPersistentHashSet::keepOldElement, Objects::equals, OldPersistentHashSet::keyHash);
        if (details.isModified()) {
            return new OldPersistentHashSet<>(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OldPersistentHashSet<E> addingAll(Iterable<? extends E> c) {
        if (isEmpty() && c instanceof OldPersistentHashSet<? extends E> s) {
            return (OldPersistentHashSet<E>) s;
        }
        var m = toMutable();
        return m.addAll(c) ? m.toPersistent() : this;
    }

    /// {@inheritDoc}
    @Override
    public OldPersistentHashSet<E> cleared() {
        return of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return root.find((E) o, keyHash(o), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof OldPersistentHashSet<?> that) {
            return size == that.size && root.equivalent(that.root);
        }
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new ChampIterator<>(root, null);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public OldPersistentHashSet<E> removing(E key) {
        int keyHash = keyHash(key);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.remove(null, key, keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            return size == 1 ? OldPersistentHashSet.of() : new OldPersistentHashSet<>(newRootNode, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OldPersistentHashSet<E> removingAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public OldPersistentHashSet<E> retainingAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<E> spliterator() {
        return new ChampSpliterator<>(root, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    @Override
    public OldMutableHashSet<E> toMutable() {
        return new OldMutableHashSet<>(this);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
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
            return OldPersistentHashSet.copyOf(deserializedElements);
        }
    }
}