/*
 * @(#)TrieList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.AbstractList;
import java.util.stream.Stream;

public class TrieList<E> extends AbstractList<E> implements ReadOnlySequencedCollection<E> {
    private TrieListHelper.RrbTree<E> tree = new TrieListHelper.RrbTree<>();

    @Override
    public boolean add(E e) {
        tree.add(e);
        return true;
    }

    @Override
    public E get(int index) {
        return tree.get(index);
    }

    @Override
    public E getFirst() {
        return null;
    }

    @Override
    public E getLast() {
        return null;
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }
}
