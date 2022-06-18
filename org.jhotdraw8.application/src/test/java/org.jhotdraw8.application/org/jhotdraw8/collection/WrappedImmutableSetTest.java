/*
 * @(#)PersistentChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class WrappedImmutableSetTest extends AbstractImmutableSetTest {


    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance() {
        Function<Set<E>, Set<E>> cloneFunction = (Function<Set<E>, Set<E>>) e ->
                (Set<E>) (Set<?>) ((LinkedHashSet<?>) e).clone();
        return new WrappedImmutableSet<>(new LinkedHashSet<>(), cloneFunction);
    }


    @Override
    protected <E> @NonNull Set<E> toMutableInstance(ImmutableSet<E> m) {
        return m.toMutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> toImmutableInstance(Set<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = (Function<Set<E>, Set<E>>) e ->
                (Set<E>) (Set<?>) ((LinkedHashSet<?>) e).clone();
        return new WrappedImmutableSet<>(m, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> toClonedInstance(ImmutableSet<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = (Function<Set<E>, Set<E>>) e ->
                (Set<E>) (Set<?>) ((LinkedHashSet<?>) e).clone();
        return new WrappedImmutableSet<>(m.toMutable(), cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = (Function<Set<E>, Set<E>>) e ->
                (Set<E>) (Set<?>) ((LinkedHashSet<?>) e).clone();
        if (m instanceof ReadOnlySet<E>) {
            return new WrappedImmutableSet<>(new LinkedHashSet<>(((ReadOnlySet<E>) m).asSet()), cloneFunction);
        }
        return new WrappedImmutableSet<>((Set<E>) m, cloneFunction);
    }


}