/*
 * @(#)ReversedObservableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

/**
 * Reversed list provides a view on an underlying list with items ordered in
 * reverse.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReversedObservableList<E> extends TransformationList<E, E> {

    private int size;

    public ReversedObservableList(@NonNull ObservableList<E> source) {
        super(source);
        size = source.size();
    }

    @Override
    protected void sourceChanged(ListChangeListener.@NonNull Change<? extends E> c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                int from = c.getFrom();
                int[] perm = new int[c.getTo() - from];
                for (int i = from; i < c.getTo(); i++) {
                    perm[i - from] = size - c.getPermutation(i);
                }
                nextPermutation(size - 1 - c.getTo(), size - 1 - c.getFrom(), perm);
            } else if (c.wasUpdated()) {
                //update item
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    nextUpdate(size - 1 - i);
                }
            } else {
                if (c.wasRemoved()) {
                    List<E> reversed = new ArrayList<>(c.getRemoved());
                    for (int i = 0, n = c.getRemovedSize(); i < n; i++) {
                        reversed.set(n - i - 1, c.getRemoved().get(i));
                    }
                    size = size - c.getRemovedSize();
                    // c.getTo() is not always correctly filled in by the underlying list!
                    nextRemove(size - c.getFrom() - c.getRemovedSize() + 1, reversed);
                }
                if (c.wasAdded()) {
                    size = size + c.getAddedSize();
                    nextAdd(size - c.getTo(), size - c.getFrom());

                }
            }
        }
        assert size == getSource().size() : "ReversedObservableList this.size=" + this.size + " sourceSize:" + getSource().size();
        endChange();
    }

    @Override
    public int getSourceIndex(int index) {
        return size - 1 - index;
    }

    //Mark as @Override when we migrate to Java 9
    //@Override
    public int getViewIndex(int index) {
        return size - 1 - index;
    }

    @Override
    public E get(int index) {
        return getSource().get(getSourceIndex(index));
    }

    @Override
    public E set(int index, E e) {
        @SuppressWarnings("unchecked")
        List<E> tmp = ((List<E>) getSource());
        return tmp.set(getSourceIndex(index), e);
    }

    @Override
    public E remove(int index) {
        return getSource().remove(getSourceIndex(index));
    }

    @Override
    public boolean remove(Object o) {
        return getSource().remove(o);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void add(int index, E element) {
        @SuppressWarnings("unchecked")
        ObservableList<E> src = (ObservableList<E>) getSource();
        src.add(size - min(size, index), element);
    }

    /**
     * Notifies all listeners of a change
     *
     * @param from start of range
     * @param to   end of range + 1
     */
    public void fireUpdated(int from, int to) {
        beginChange();
        for (int i = from; i < to; i++) {
            nextUpdate(i);
        }
        endChange();
    }

}
