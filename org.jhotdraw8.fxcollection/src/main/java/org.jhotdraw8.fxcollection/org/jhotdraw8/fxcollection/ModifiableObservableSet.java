/*
 * @(#)ModifiableObservableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This ObservableSet implementation provides overridable fire methods, saving one
 * level of indirection.
 *
 * @param <E> the element type
 */
public class ModifiableObservableSet<E> extends AbstractObservableSet<E> {

    private Set<E> backingSet;

    public ModifiableObservableSet(Collection<E> copyMe) {
        backingSet = new LinkedHashSet<>(copyMe);
    }

    public void setBackingSet(Set<E> backingSet) {
        this.backingSet = backingSet;
    }

    public ModifiableObservableSet() {
    }


    @Override
    protected boolean backingSetAdd(E e) {
        makeBackingSet();
        return backingSet.add(e);
    }

    private void makeBackingSet() {
        if (backingSet == null) {
            backingSet = new LinkedHashSet<>();
        }
    }

    @Override
    protected void backingSetClear() {
        if (backingSet != null) {
            backingSet.clear();
        }
    }

    @Override
    protected Object[] backingSetToArray() {
        return backingSet == null ? new Object[0] : backingSet.toArray();
    }

    @Override
    protected boolean backingSetContains(Object o) {
        return backingSet != null && backingSet.contains(o);
    }

    @Override
    protected boolean backingSetContainsAll(Collection<?> c) {
        return backingSet != null && backingSet.containsAll(c);
    }

    @Override
    protected boolean backingSetIsEmpty() {
        return backingSet == null || backingSet.isEmpty();
    }

    @Override
    protected Iterator<E> backingSetIterator() {
        return backingSet == null ? Collections.emptyIterator() : backingSet.iterator();
    }

    @Override
    protected boolean backingSetRemove(Object o) {
        return backingSet != null && backingSet.remove(o);
    }

    @Override
    protected int backingSetSize() {
        return backingSet == null ? 0 : backingSet.size();
    }
}
