/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/// Implements the [Set] interface using a Compressed Hash-Array Mapped
/// Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is mutable
///   - is not thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N)
///   - remove: O(log₃₂ N)
///   - contains: O(log₃₂ N)
///   - toPersistent: O(1) + O(log₃₂ N) distributed across subsequent updates in
///     this set
///   - clone: O(1) + O(log₃₂ N) distributed across subsequent updates in this
///     set and in the clone
///   - iterator.next: O(1)
///
///
/// Implementation details:
///
/// See description at [PersistentLinkedHashSetWithNodeSubClasses].
///
/// References:
///
/// Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>Michael J. Steindorfer (2017).
///      Efficient Persistent Collections.</dt>
///      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
/// </dl>
///
/// @param <E> the element type
public class MutableLinkedHashSet<E> extends AbstractSet<E> implements SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;
    private PersistentLinkedHashSetWithNodeSubClasses<E> delegate;
    private int modCount;
    private IdentityObject owner;

    /// Constructs a new empty set.
    public MutableLinkedHashSet() {
        delegate = PersistentLinkedHashSetWithNodeSubClasses.<E>of();
    }

    /// Constructs a set containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableLinkedHashSet(Iterable<? extends E> c) {
        this();
        if (c instanceof PersistentLinkedHashSetWithNodeSubClasses) {
            delegate = (PersistentLinkedHashSetWithNodeSubClasses<E>) c;
        } else {
            addAll(c);
        }
    }

    /// Adds all specified elements that are not already in this set.
    ///
    /// @param c an iterable of elements
    /// @return `true` if this set changed
    @SuppressWarnings("unchecked")
    public boolean addAll(Iterable<? extends E> c) {
        boolean added = false;
        for (E e : c) {
            added |= add(e);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        var newDelegate = delegate.remove((E) o, owner);
        if (newDelegate != null) {
            modCount++;
            this.delegate = newDelegate;
            return true;
        }
        return false;
    }

    /// Retains all specified elements that are in this set.
    ///
    /// @param c an iterable of elements
    /// @return `true` if this set changed
    public boolean retainAll(Iterable<?> c) {
        if (c == this || isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        Predicate<E> predicate;
        if (c instanceof Collection<?> that) {
            predicate = that::contains;
        } else if (c instanceof ReadableCollection<?> that) {
            predicate = that::contains;
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            predicate = that::contains;
        }
        boolean removed = false;
        for (Iterator<E> i = iterator(); i.hasNext(); ) {
            E e = i.next();
            if (!predicate.test(e)) {
                i.remove();
                removed = true;
            }
        }
        return removed;
    }

    /// Removes all specified elements that are in this set.
    ///
    /// @param c an iterable of elements
    /// @return `true` if this set changed
    public boolean removeAll(Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(delegate.iterator(),
                this::iteratorRemove, this::getModCount);
    }

    @Override
    public boolean add(E e) {
        var newDelegate = delegate.addLast(e, false, owner);
        if (newDelegate != delegate) {
            this.modCount++;
            this.delegate = newDelegate;
            return true;
        }
        return false;
    }

    @Override
    public void addFirst(E e) {
        var newDelegate = delegate.addFirst(e, true, owner);
        if (newDelegate != delegate) {
            this.modCount++;
            this.delegate = newDelegate;
        }
    }

    @Override
    public void addLast(E e) {
        var newDelegate = delegate.addLast(e, true, owner);
        if (newDelegate != delegate) {
            this.modCount++;
            this.delegate = newDelegate;
        }
    }

    @Override
    public E getFirst() {
        return delegate.getFirst();
    }

    @Override
    public E getLast() {
        return delegate.getLast();
    }

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public PersistentLinkedHashSetWithNodeSubClasses<E> toPersistent() {
        owner = new IdentityObject();
        return delegate;
    }

    @Override
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, this::reverseIterator,
                this::reverseSpliterator);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    private Iterator<E> reverseIterator() {
        return new FailFastIterator<>(
                delegate.reverseIterator(),
                this::iteratorRemove, this::getModCount
        );
    }

    protected int getModCount() {
        return modCount;
    }

    private Spliterator<E> reverseSpliterator() {
        return Spliterators.spliterator(reverseIterator(), size(), Spliterator.SIZED | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT);
    }


    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this);
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
            return new MutableLinkedHashSet<>(deserializedElements);
        }
    }
}