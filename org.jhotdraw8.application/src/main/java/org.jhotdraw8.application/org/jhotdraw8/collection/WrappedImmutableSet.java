package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class WrappedImmutableSet<E> extends AbstractReadOnlySet<E> implements ImmutableSet<E> {
    private final @NonNull Set<E> target;
    private final @NonNull Function<Set<E>, Set<E>> cloneFunction;

    public WrappedImmutableSet(@NonNull Set<E> target, @NonNull Function<Set<E>, Set<E>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @Override
    public @NonNull ImmutableSet<E> copyClear() {
        if (isEmpty()) {
            return this;
        }
        Set<E> clone = cloneFunction.apply(target);
        clone.clear();
        return new WrappedImmutableSet<>(clone, cloneFunction);
    }

    @Override
    public @NonNull ImmutableSet<E> copyAdd(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.add(element) ? new WrappedImmutableSet<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> copyAddAll(@NonNull Iterable<? extends E> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (E e : c) {
            changed |= clone.add(e);
        }
        return changed ? new WrappedImmutableSet<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> copyRemove(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.remove(element) ? new WrappedImmutableSet<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> copyRemoveAll(@NonNull Iterable<?> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (Object e : c) {
            changed |= clone.remove(e);
        }
        return changed ? new WrappedImmutableSet<>(clone, cloneFunction) : this;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public @NonNull ImmutableSet<E> copyRetainAll(@NonNull Collection<?> c) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.retainAll(c) ? new WrappedImmutableSet<>(clone, cloneFunction) : this;
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean contains(Object o) {
        return target.contains(o);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(target.iterator());
    }

    @Override
    public @NonNull Set<E> toMutable() {
        return cloneFunction.apply(target);
    }


}
