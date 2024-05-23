/*
 * @(#)IndexedHashObservableSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcollection.indexedset;


import java.util.Set;

public class IndexedHashObservableSetTest extends AbstractSetTestOld {
    @Override
    protected <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new IndexedHashObservableSet<>();
    }
}
