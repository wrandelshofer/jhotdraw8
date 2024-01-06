package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.navigable.DescendingNavigableSetView;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.serialization.SortedSetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Spliterator;

/**
 * Implements the {@link NavigableSet} interface using a Red-Black tree.
 * <p>
 * References:
 * <p>
 * For a similar design, see 'TreeSet.java' in vavr. The internal data structure of
 * this class is licensed from vavr.
 * <dl>
 *     <dt>TreeSet.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/TreeSet.java">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class MutableRedBlackSet<E> extends AbstractSet<E> implements NavigableSet<E>, Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * The number of times this set has been structurally modified.
     */
    protected transient int modCount;
    final @NonNull Comparator<E> comparator;
    @NonNull RedBlackTree<E> root;

    /**
     * Constructs a new, empty set, sorted according to the
     * specified comparator.
     *
     * @param comparator the comparator
     */
    public MutableRedBlackSet(@NonNull Comparator<E> comparator) {
        this.comparator = comparator;
        this.root = RedBlackTree.empty();
    }

    /**
     * Constructs a new tree set containing the elements in the specified
     * collection, sorted according to the specified comparator.
     *
     * @param comparator the comparator
     * @param c          the collection
     */
    public MutableRedBlackSet(@NonNull Comparator<E> comparator, Collection<? extends E> c) {
        this.comparator = comparator;
        this.root = RedBlackTree.empty();
        this.addAll(c);
    }

    /**
     * Constructs a new, empty set, sorted according to the
     * <i>natural ordering</i> of its elements.
     */
    public MutableRedBlackSet() {
        this.comparator = NaturalComparator.instance();
        this.root = RedBlackTree.empty();
    }

    /**
     * Constructs a new tree set containing the elements in the specified
     * collection, sorted according to the <i>natural ordering</i> of its
     * elements.
     *
     * @param c the collection
     */
    public MutableRedBlackSet(Iterable<? extends E> c) {
        this.comparator = NaturalComparator.instance();
        this.root = RedBlackTree.empty();
        this.addAll(c);
    }

    MutableRedBlackSet(@NonNull Comparator<E> comparator, RedBlackTree<E> root) {
        this.comparator = comparator;
        this.root = root;
    }

    @Override
    public boolean add(E e) {
        RedBlackTree<E> newRoot = root.insert(e, comparator, RedBlackSet::updateElement);
        if (newRoot.size() != root.size()) {
            root = newRoot;
            modCount++;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        if (c instanceof MutableRedBlackSet<? extends E> r && r.comparator == this.comparator) {
            RedBlackTree<E> newRoot = root.addAll((RedBlackTree<E>) r.root, comparator, RedBlackSet::updateElement);
            if (newRoot.size() != root.size()) {
                root = newRoot;
                modCount++;
                return true;
            }
            return false;
        }
        return super.addAll(c);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c instanceof MutableRedBlackSet<?> m) {
            c = (Iterable<? extends E>) m.toImmutable();
        }
        if (isEmpty() && (c instanceof RedBlackSet<?> cc && cc.comparator == this.comparator)) {
            root = (RedBlackTree<E>) cc.root;
            modCount++;
            return true;
        }
        if (c instanceof RedBlackSet<?> that && that.comparator == this.comparator) {
            RedBlackTree<E> newRoot = root.addAll((RedBlackTree<E>) that.root, comparator, RedBlackSet::updateElement);
            if (newRoot.size() != root.size()) {
                this.root = newRoot;
                modCount++;
                return true;
            }
            return false;
        }
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return root.ceiling(e, comparator).orNull();
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            modCount++;
            root = RedBlackTree.empty();
        }
    }

    @Override
    public MutableRedBlackSet<E> clone() {
        try {
            return (MutableRedBlackSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return root.contains((E) o, comparator);
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return new FailFastIterator<>(
                root.reverseIterator(),
                this::iteratorRemove, this::getModCount
        );
    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingNavigableSetView<>(this, this::getModCount);
    }

    @Override
    public E first() {
        return root.min().orThrow();
    }

    @Nullable
    @Override
    public E floor(E e) {
        return null;
    }

    /**
     * Returns the current value of the modification counter.
     *
     * @return value of modification counter
     */
    protected int getModCount() {
        return modCount;
    }

    @NonNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return null;
    }

    @NonNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Nullable
    @Override
    public E higher(E e) {
        return root.higher(e, comparator).orNull();
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                root.iterator(),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(
                NavigableSet.super.spliterator(),
                this::getModCount,
                comparator == NaturalComparator.instance() ? null : comparator);
    }

    @Override
    public E last() {
        return root.max().orThrow();
    }

    @Nullable
    @Override
    public E lower(E e) {
        return root.lower(e, comparator).orNull();
    }

    @Nullable
    @Override
    public E pollFirst() {
        E value = root.min().orNull();
        root = root.delete(value, comparator);
        return value;
    }

    @Nullable
    @Override
    public E pollLast() {
        E value = root.max().orNull();
        root = root.delete(value, comparator);
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        RedBlackTree<E> newRoot = root.delete((E) o, comparator);
        if (newRoot.size() != root.size()) {
            root = newRoot;
            modCount++;
            return true;
        }
        return false;
    }


    @Override
    public int size() {
        return root.size();
    }

    @NonNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return null;
    }

    @NonNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @NonNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return null;
    }

    @NonNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }


    /**
     * Returns an immutable copy of this set.
     *
     * @return an immutable copy
     */
    public @NonNull RedBlackSet<E> toImmutable() {
        return new RedBlackSet<>(comparator, root);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new MutableRedBlackSet.SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SortedSetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull SortedSet<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return new MutableRedBlackSet<>(deserializedComparator, deserializedElements);
        }
    }

    @Override
    public String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

}
