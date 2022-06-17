/*
 * @(#)SeqChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class ImmutableSequencedChampSetSequencedSetTest extends AbstractSequencedSetTestOld {

    @Override
    protected @NonNull <T> SequencedSet<T> create(int expectedMaxSize, float maxLoadFactor) {
        class MySet<E> extends AbstractSet<E> implements SequencedSet<E> {
            private ImmutableSequencedSet<E> target;

            public MySet(ImmutableSequencedSet<E> target) {
                this.target = target;
            }

            @Override
            public SequencedSet<E> reversed() {
                return new WrappedSequencedSet<E>(
                        () -> target.readOnlyReversed().iterator(),
                        target::iterator,
                        target::size,
                        target::contains
                );
            }

            @Override
            public String toString() {
                return target.toString();
            }
/*
            @Override
            public boolean equals(Object o) {
                return target.equals(o);
            }*/

            @Override
            public int hashCode() {
                return target.hashCode();
            }

            @Override
            public void addFirst(E e) {
                target = target.copyAddFirst(e);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                ImmutableSequencedSet<E> newTarget = target.copyRemoveAll(c);
                boolean modified = newTarget != target;
                target = newTarget;
                return modified;
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                ImmutableSequencedSet<E> newTarget = target.copyAddAll(c);
                boolean modified = newTarget != target;
                target = newTarget;
                return modified;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                ImmutableSequencedSet<E> newTarget = target.copyRetainAll(c);
                boolean modified = newTarget != target;
                target = newTarget;
                return modified;
            }

            @Override
            public boolean contains(Object o) {
                return target.contains(o);
            }

            @Override
            public boolean add(E e) {
                ImmutableSequencedSet<E> newTarget = target.copyAdd(e);
                boolean modified = newTarget != target;
                target = newTarget;
                return modified;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean remove(Object o) {
                ImmutableSequencedSet<E> newTarget = target.copyRemove((E) o);
                boolean modified = newTarget != target;
                target = newTarget;
                return modified;
            }

            @Override
            public void addLast(E e) {
                target = target.copyAddLast(e);
            }

            @Override
            public E getLast() {
                return target.getLast();
            }

            @Override
            public Iterator<E> iterator() {
                return new FailFastIterator<>(target.iterator(),
                        () -> 0) {
                    E current;
                    boolean canRemove;

                    @Override
                    public E next() {
                        current = super.next();
                        canRemove = true;
                        return current;
                    }

                    @Override
                    public void remove() {
                        super.ensureUnmodified();
                        if (!canRemove) {
                            throw new IllegalStateException();
                        }
                        target = target.copyRemove(current);
                        canRemove = false;
                    }
                };
            }

            @Override
            public E removeLast() {
                E e = target.getLast();
                target = target.copyRemoveLast();
                return e;
            }

            @Override
            public int size() {
                return target.size();
            }
        }
        ;
        return new MySet<>(ImmutableSequencedChampSet.of());
        //return new SequencedChampSet<>();
    }
}
