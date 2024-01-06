/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.champ.AbstractMutableChampSet;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.BulkChangeEvent;
import org.jhotdraw8.icollection.impl.champ.ChampIterator;
import org.jhotdraw8.icollection.impl.champ.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Implements the {@link Set} interface using a Compressed Hash-Array Mapped
 * Prefix-tree (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 elements</li>
 *     <li>allows null elements</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(log₃₂ N)</li>
 *     <li>remove: O(log₃₂ N)</li>
 *     <li>contains: O(log₃₂ N)</li>
 *     <li>toImmutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
 *     this set</li>
 *     <li>clone: O(1) + O(log₃₂ N) distributed across subsequent updates in this
 *     set and in the clone</li>
 *     <li>iterator.next: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link ChampSet}.
 * <p>
 * References:
 * <p>
 * Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
public class MutableChampSet<E> extends AbstractMutableChampSet<E, E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new empty set.
     */
    public MutableChampSet() {
        root = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableChampSet(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableChampSet<?>) {
            c = ((MutableChampSet<? extends E>) c).toImmutable();
        }
        if (c instanceof ChampSet<?>) {
            ChampSet<E> that = (ChampSet<E>) c;
            this.root = that.root;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            addAll(c);
        }
    }

    @Override
    public boolean add(@Nullable E e) {
        ChangeEvent<E> details = new ChangeEvent<>();
        root = root.put(makeOwner(),
                e, ChampSet.keyHash(e), 0, details,
                (oldKey, newKey) -> oldKey,
                Objects::equals, ChampSet::keyHash);
        if (details.isModified()) {
            size++;
            modCount++;
        }
        return details.isModified();
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableChampSet<?> m) {
            c = (Iterable<? extends E>) m.toImmutable();
        }
        if (isEmpty() && (c instanceof ChampSet<?> cc)) {
            root = (BitmapIndexedNode<E>) cc.root;
            size = cc.size;
            return true;
        }
        if (c instanceof ChampSet<?> that) {
            var bulkChange = new BulkChangeEvent();
            var newRootNode = root.putAll(makeOwner(), (Node<E>) that.root, 0, bulkChange, ChampSet::updateElement, Objects::equals, ChampSet::keyHash, new ChangeEvent<>());
            if (bulkChange.inBoth == that.size()) {
                return false;
            }
            root = newRootNode;
            size += that.size - bulkChange.inBoth;
            modCount++;
            return true;
        }
        return super.addAll(c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    @Override
    public boolean removeAll(@NonNull Iterable<?> c) {
        if (isEmpty()
                || (c instanceof Collection<?> cc) && cc.isEmpty()
                || (c instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        if (c instanceof MutableChampSet<?> m) {
            c = m.toImmutable();
        }
        if (c instanceof ChampSet<?> that) {
            BulkChangeEvent bulkChange = new BulkChangeEvent();
            BitmapIndexedNode<E> newRootNode = root.removeAll(makeOwner(), (BitmapIndexedNode<E>) that.root, 0, bulkChange, ChampSet::updateElement, Objects::equals, ChampSet::keyHash, new ChangeEvent<>());
            if (bulkChange.removed == 0) {
                return false;
            }
            root = newRootNode;
            size -= bulkChange.removed;
            modCount++;
            return true;
        }
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        if (isEmpty()) {
            return false;
        }
        if (c.isEmpty()) {
            clear();
            return true;
        }
        if (c instanceof MutableChampSet<?> m) {
            ChampSet<?> that = m.toImmutable();
            BulkChangeEvent bulkChange = new BulkChangeEvent();
            BitmapIndexedNode<E> newRootNode = root.retainAll(makeOwner(), (BitmapIndexedNode<E>) that.root, 0, bulkChange, ChampSet::updateElement, Objects::equals, ChampSet::keyHash, new ChangeEvent<>());
            if (bulkChange.removed == 0) {
                return false;
            }
            root = newRootNode;
            size -= bulkChange.removed;
            modCount++;
            return true;
        }
        return super.retainAll(c);
    }


    @SuppressWarnings("unchecked")
    public boolean retainAll(@NonNull Iterable<?> c) {
        if (c == this || isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        BitmapIndexedNode<E> newRootNode;
        if (c instanceof ChampSet<?> that) {
            newRootNode = root.retainAll(makeOwner(), (BitmapIndexedNode<E>) that.root, 0, bulkChange, ChampSet::updateElement, Objects::equals, ChampSet::keyHash, new ChangeEvent<>());
        } else if (c instanceof Collection<?> that) {
            newRootNode = root.filterAll(makeOwner(), that::contains, 0, bulkChange);
        } else if (c instanceof ReadOnlyCollection<?> that) {
            newRootNode = root.filterAll(makeOwner(), that::contains, 0, bulkChange);
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            newRootNode = root.filterAll(makeOwner(), that::contains, 0, bulkChange);
        }
        if (bulkChange.removed == 0) {
            return false;
        }
        root = newRootNode;
        size -= bulkChange.removed;
        modCount++;
        return true;
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    public @NonNull MutableChampSet<E> clone() {
        return (MutableChampSet<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return Node.NO_DATA != root.find((E) o, ChampSet.keyHash(o), 0, Objects::equals);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new FailFastIterator<>(
                new ChampIterator<E, E>(root, null),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(new ChampSpliterator<>(root, Function.identity(), size, Spliterator.DISTINCT | Spliterator.SIZED), () -> this.modCount, null);
    }

    private void iteratorRemove(E e) {
        owner = null;
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<E> details = new ChangeEvent<>();
        root = root.remove(makeOwner(),
                (E) o, ChampSet.keyHash(o), 0, details,
                Objects::equals);
        if (details.isModified()) {
            size--;
            modCount++;
        }
        return details.isModified();
    }

    /**
     * Returns an immutable copy of this set.
     *
     * @return an immutable copy
     */
    public @NonNull ChampSet<E> toImmutable() {
        owner = null;
        return size == 0
                ? ChampSet.of()
                : new ChampSet<>(root, size);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return new MutableChampSet<>(deserialized);
        }
    }
}