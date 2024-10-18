/*
 * @(#)ImmutableSetFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.AbstractImmutableSetTest;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class ImmutableSetFacadeTest extends AbstractImmutableSetTest {


    @SuppressWarnings("unchecked")
    @Override
    protected <E> PersistentSet<E> newInstance() {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new PersistentSetFacade<>(new LinkedHashSet<>(), cloneFunction);
    }


    @Override
    protected <E> Set<E> toMutableInstance(PersistentSet<E> m) {
        return m.toMutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> PersistentSet<E> toImmutableInstance(Set<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new PersistentSetFacade<>(m, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> PersistentSet<E> toClonedInstance(PersistentSet<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        return new PersistentSetFacade<>(m.toMutable(), cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> PersistentSet<E> newInstance(Iterable<E> m) {
        Function<Set<E>, Set<E>> cloneFunction = e ->
                (Set<E>) ((LinkedHashSet<?>) e).clone();
        if (m instanceof ReadableSet<E>) {
            return new PersistentSetFacade<>(new LinkedHashSet<>(((ReadableSet<E>) m).asSet()), cloneFunction);
        }
        return new PersistentSetFacade<>((Set<E>) m, cloneFunction);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}