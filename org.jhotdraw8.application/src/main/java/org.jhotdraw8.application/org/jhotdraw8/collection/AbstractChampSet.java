package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Abstract base class for CHAMP sets.
 *
 * @param <E> the element type of the set
 * @param <X> the key type of the CHAMP trie
 */
abstract class AbstractChampSet<E, X> extends AbstractSet<E> implements Serializable, Cloneable,
        ReadOnlySet<E> {
    private final static long serialVersionUID = 0L;
    protected @Nullable UniqueId mutator;
    protected BitmapIndexedNode<X> root;
    protected int size;
    protected transient int modCount;

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return addAll((Iterable<? extends E>) c);
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c == this) {
            return false;
        }
        boolean modified = false;
        for (E e : c) {
            modified |= add(e);
        }
        return modified;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof AbstractChampSet<?, ?>) {
            return root.equivalent(((AbstractChampSet<?, ?>) o).root);
        }
        return super.equals(o);
    }

    @Override
    public int size() {
        return size;
    }

    protected @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean removeAll(@NonNull Iterable<?> c) {
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
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull AbstractChampSet<E, X> clone() {
        try {
            mutator = null;
            return (AbstractChampSet<E, X>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
