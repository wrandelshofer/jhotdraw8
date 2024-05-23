/*
 * @(#)ImmutableSetFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.AbstractImmutableSetTest;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class ImmutableSetFacadeTest extends AbstractImmutableSetTest {


    @SuppressWarnings("unchecked")
    @Override
    protected <E> ImmutableSet<E> newInstance() {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(new LinkedHashSet<>(), cloneFunction);
    }


    @Override
    protected <E> Set<E> toMutableInstance(ImmutableSet<E> m) {
        return m.toMutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> ImmutableSet<E> toImmutableInstance(Set<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(m, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> ImmutableSet<E> toClonedInstance(ImmutableSet<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new ImmutableSetFacade<>(m.toMutable(), cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> ImmutableSet<E> newInstance(Iterable<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        if (m instanceof ReadOnlySet<E>) {
            return new ImmutableSetFacade<>(new LinkedHashSet<>(((ReadOnlySet<E>) m).asSet()), cloneFunction);
        }
        return new ImmutableSetFacade<>((Set<E>) m, cloneFunction);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}