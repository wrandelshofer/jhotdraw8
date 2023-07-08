/*
 * @(#)ImmutableSetFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.AbstractImmutableSetTest;
import org.jhotdraw8.pcollection.immutable.ImmutableSet;
import org.jhotdraw8.pcollection.readonly.ReadOnlySet;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class ImmutableSetFacadeTest extends AbstractImmutableSetTest {


    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance() {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(new LinkedHashSet<>(), cloneFunction);
    }


    @Override
    protected <E> @NonNull Set<E> toMutableInstance(ImmutableSet<E> m) {
        return m.toMutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> toImmutableInstance(Set<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(m, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> toClonedInstance(ImmutableSet<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(m.toMutable(), cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        if (m instanceof ReadOnlySet<E>) {
            return new ImmutableSetFacade<>(new LinkedHashSet<>(((ReadOnlySet<E>) m).asSet()), cloneFunction);
        }
        return new ImmutableSetFacade<>((Set<E>) m, cloneFunction);
    }


}