/*
 * @(#)AbstractObservableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Abstract base class for {@link ObservableSet}s. This implementation provides
 * overridable fire methods, saving one level of indirection.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public abstract class AbstractObservableSet<E> extends AbstractSet<E> implements ObservableSet<E>, ReadOnlySet<E> {

    private final List<SetChangeListener<? super E>> changeListeners = new CopyOnWriteArrayList<>();
    private final List<InvalidationListener> invalidationListeners = new CopyOnWriteArrayList<>();

    public AbstractObservableSet() {
    }


    @Override
    public boolean add(E e) {
        boolean modified = backingSetAdd(e);
        if (modified) {
            fireAdded(e);
            fireInvalidated();
        }
        return modified;
    }

    protected abstract boolean backingSetAdd(E e);

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            boolean added = backingSetAdd(e);
            if (added) {
                fireAdded(e);
                modified = true;
            }
        }
        if (modified) {
            fireInvalidated();
        }
        return modified;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener) {
        changeListeners.add(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        @SuppressWarnings("assignment.type.incompatible")
        Object[] values = backingSetToArray();
        backingSetClear();
        for (Object v : values) {
            fireRemoved((E) v);
        }
        if (values.length > 0) {
            fireInvalidated();
        }
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    protected abstract void backingSetClear();

    protected abstract Object[] backingSetToArray();


    @Override
    public boolean contains(@Nullable Object o) {
        return backingSetContains(o);
    }

    protected abstract boolean backingSetContains(Object o);

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return backingSetContainsAll(c);
    }

    protected abstract boolean backingSetContainsAll(Collection<?> c);

    protected void fireAdded(@Nullable E e) {
        SetChangeListener.Change<E> change = null;
        for (SetChangeListener<? super E> listener : changeListeners) {
            if (change == null) {
                change = new Change<>(this, e, true);
            }
            listener.onChanged(change);
        }
    }

    private void fireInvalidated() {
        invalidated();
        for (InvalidationListener l : invalidationListeners) {
            l.invalidated(this);
        }
    }

    protected void fireRemoved(@Nullable E e) {
        SetChangeListener.Change<E> change = null;
        for (SetChangeListener<? super E> listener : changeListeners) {
            if (change == null) {
                change = new Change<>(this, e, false);
            }
            listener.onChanged(change);
        }
    }

    public void fireUpdated(@Nullable E e) {
        fireRemoved(e);
        fireAdded(e);
        fireInvalidated();
    }

    /**
     * The method {@code invalidated()} can be overridden to receive
     * invalidation notifications. This is the preferred option in
     * {@code Objects} defining the property, because it requires less memory.
     * <p>
     * The default implementation is empty.
     */
    protected void invalidated() {
    }

    @Override
    public boolean isEmpty() {
        return backingSetIsEmpty();
    }

    protected abstract boolean backingSetIsEmpty();

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<>() {
            private final Iterator<? extends E> i = backingSetIterator();
            private @Nullable E current;

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public E next() {
                return current = i.next();
            }

            @Override
            public void remove() {
                i.remove();
                fireRemoved(current);
                fireInvalidated();
            }
        };
    }

    protected abstract Iterator<E> backingSetIterator();

    @Override
    public boolean remove(@Nullable Object o) {
        boolean modified = backingSetRemove(o);
        if (modified) {
            @SuppressWarnings("unchecked") final E e = (E) o;
            fireRemoved(e);
            fireInvalidated();
        }
        return modified;
    }

    protected abstract boolean backingSetRemove(Object o);

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            @SuppressWarnings("unchecked") final E e = (E) o;
            boolean removed = backingSetRemove(e);
            if (removed) {
                fireRemoved(e);
                modified = true;
            }
        }
        if (modified) {
            fireInvalidated();
        }
        return modified;
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listener) {
        changeListeners.remove(listener);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        boolean modified = false;
        Iterator<E> it = backingSetIterator();
        while (it.hasNext()) {
            E e = it.next();
            if (!c.contains(e)) {
                it.remove();
                fireRemoved(e);
                modified = true;
            }
        }
        if (modified) {
            fireInvalidated();
        }
        return modified;
    }

    @Override
    public int size() {
        return backingSetSize();
    }

    protected abstract int backingSetSize();

    private static class Change<EE> extends SetChangeListener.Change<EE> {

        private final @Nullable EE value;
        private final boolean wasAdded;

        public Change(ObservableSet<EE> set, @Nullable EE value, boolean wasAdded) {
            super(set);
            this.value = value;
            this.wasAdded = wasAdded;
        }

        @Override
        @SuppressWarnings("override.return.invalid")
        public @Nullable EE getElementAdded() {
            return (wasAdded) ? value : null;
        }

        @Override
        @SuppressWarnings("override.return.invalid")
        public @Nullable EE getElementRemoved() {
            return (!wasAdded) ? value : null;
        }

        @Override
        public boolean wasAdded() {
            return wasAdded;
        }

        @Override
        public boolean wasRemoved() {
            return !wasAdded;
        }

    }

}
