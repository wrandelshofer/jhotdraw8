package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.FailFastIterator;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other sets, and nodes
 * that are exclusively owned by this set.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * set is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * set is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This set can create an immutable copy of itself in O(1) time and O(1) space
 * using method {@link #toImmutable()}. This set loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating an immutable copy increases the constant cost of
 * subsequent writes, until all shared nodes have been gradually replaced by
 * exclusively owned nodes again.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access this set concurrently, and at least
 * one of the threads modifies the set, it <em>must</em> be synchronized
 * externally.  This is typically accomplished by synchronizing on some
 * object that naturally encapsulates the set.
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
public class MutableChampSet<E> extends AbstractMutableChampSet<E> {
    private static final long serialVersionUID = 0L;
    private @NonNull ChampSet<E> set = ChampSet.of();

    /**
     * Constructs a new empty set.
     */
    public MutableChampSet() {
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableChampSet(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableChampSet<?>) {
            set = ((MutableChampSet<E>) c).toImmutable();
        }
        if (c instanceof ChampSet<?>) {
            set = (ChampSet<E>) c;
        } else {
            addAll(c);
        }
    }

    @Override
    public boolean add(@Nullable E e) {
        var oldSet = set;
        set = set.add(e, createIdentity());
        return wrap(oldSet);
    }

    private boolean wrap(ChampSet<E> oldSet) {
        if (oldSet != set) {
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        var oldSet = set;
        set = set.addAll(c, createIdentity());
        return wrap(oldSet);
    }

    @Override
    public boolean removeAll(@NonNull Iterable<?> c) {
        var oldSet = set;
        set = set.removeAll(c, createIdentity());
        return wrap(oldSet);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        var oldSet = set;
        set = set.retainAll(c);
        return wrap(oldSet);
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        set = ChampSet.of();
        modCount++;
    }

    @Override
    public MutableChampSet<E> clone() {
        return (MutableChampSet<E>) super.clone();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }


    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                new KeyIterator<>(set, this::iteratorRemove),
                () -> this.modCount);
    }

    void iteratorRemove(E e) {
        mutator = null;
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(@Nullable Object e) {
        var oldSet = set;
        set = set.remove((E) e, createIdentity());
        return wrap(oldSet);
    }

    @Override
    public int size() {
        return set.size();
    }


    /**
     * Returns an immutable copy of this set.
     *
     * @return an immutable copy
     */
    public @NonNull ChampSet<E> toImmutable() {
        mutator = null;
        return set;
    }

    private @NonNull Object writeReplace() {
        return new MutableChampSet.SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new MutableChampSet<>(deserialized);
        }
    }
}
