package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.stream.Stream;

abstract class AbstractMutableChampSet<E> extends AbstractSet<E>
        implements Serializable, Cloneable, ReadOnlySet<E> {
    private static final long serialVersionUID = 0L;

    /**
     * The current mutator id of this set.
     * <p>
     * All nodes that have the same non-null mutator id, are exclusively owned
     * by this set, and therefore can be mutated without affecting other sets.
     * <p>
     * If this mutator id is null, then this set does not own any nodes.
     */
    protected @Nullable IdentityObject mutator;

    /**
     * The number of times this set has been structurally modified.
     */
    protected transient int modCount;


    @Override
    public final boolean addAll(@NonNull Collection<? extends E> c) {
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

    /**
     * Gets the mutator id of this set. Creates a new id, if this
     * set has no mutator id.
     *
     * @return a new unique id or the existing unique id.
     */
    @NonNull IdentityObject createIdentity() {
        if (mutator == null) {
            mutator = new IdentityObject();
        }
        return mutator;
    }

    @Override
    public final boolean removeAll(@NonNull Collection<?> c) {
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
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    /**
     * Returns a string representation of this set.
     *
     * @return a string representation of this set
     */
    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull AbstractMutableChampSet<E> clone() {
        try {
            mutator = null;
            return (AbstractMutableChampSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
