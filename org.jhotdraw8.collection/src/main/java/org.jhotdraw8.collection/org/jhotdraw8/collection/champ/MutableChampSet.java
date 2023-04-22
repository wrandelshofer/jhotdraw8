/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.FailFastIterator;
import org.jhotdraw8.collection.FailFastSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
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
 * See description at {@link ChampSet}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
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
        root = root.update(getOrCreateIdentity(),
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
        mutator = null;//XXX we should do this only once!
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<E> details = new ChangeEvent<>();
        root = root.remove(
                getOrCreateIdentity(), (E) o, Objects.hashCode(o), 0, details,
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
        mutator = null;
        return size == 0 ? ChampSet.of() : new ChampSet<>(root, size);
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