/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.impl.champ2.AbstractMutableChampSet;
import org.jhotdraw8.collection.impl.champ2.BitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ2.ChampSpliterator;
import org.jhotdraw8.collection.impl.champ2.ChangeEvent;
import org.jhotdraw8.collection.impl.champ2.Node;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Implements a mutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> elements</li>
 *     <li>allows null elements</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toImmutable: O(1) + O(log N) distributed across subsequent updates in
 *     this set</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     set and in the clone</li>
 *     <li>iterator.next: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link ChampSet2}.
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
public class MutableChampSet2<E> extends AbstractMutableChampSet<E, E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new empty set.
     */
    public MutableChampSet2() {
        root = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableChampSet2(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableChampSet2<?>) {
            c = ((MutableChampSet2<? extends E>) c).toImmutable();
        }
        if (c instanceof ChampSet2<?>) {
            ChampSet2<E> that = (ChampSet2<E>) c;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            addAll(c);
        }
    }

    @Override
    public boolean add(@Nullable E e) {
        ChangeEvent<E> details = new ChangeEvent<>();
        root = root.update(
                e, Objects.hashCode(e), 0, details,
                (oldKey, newKey) -> oldKey,
                Objects::equals, Objects::hashCode);
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
        if (c == this || c == root) {
            return false;
        }
        if (isEmpty() && (c instanceof ChampSet2<?> cc)) {
            root = (BitmapIndexedNode<E>) cc;
            size = cc.size();
            modCount++;
            return true;
        }
        boolean modified = false;
        for (E e : c) {
            modified |= add(e);
        }
        return modified;
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
    public @NonNull MutableChampSet2<E> clone() {
        return (MutableChampSet2<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return Node.NO_DATA != root.find((E) o, Objects.hashCode(o), 0, Objects::equals);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new FailFastIterator<>(
                new IteratorFacade<>(new ChampSpliterator<>(root, Function.identity(), Spliterator.DISTINCT | Spliterator.SIZED, size), this::iteratorRemove),
                () -> this.modCount);
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(new ChampSpliterator<>(root, Function.identity(), Spliterator.DISTINCT | Spliterator.SIZED, size), () -> this.modCount);
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<E> details = new ChangeEvent<>();
        root = root.remove(
                (E) o, Objects.hashCode(o), 0, details,
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
    public @NonNull ChampSet2<E> toImmutable() {
        return size == 0
                ? ChampSet2.of()
                : root instanceof ChampSet2<E> c ? c : new ChampSet2<>(root, size);
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
            return new MutableChampSet2<>(deserialized);
        }
    }
}