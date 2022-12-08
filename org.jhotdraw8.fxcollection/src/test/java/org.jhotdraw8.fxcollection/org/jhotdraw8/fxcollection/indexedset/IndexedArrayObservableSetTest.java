/*
 * @(#)IndexedArrayObservableSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcollection.indexedset;

import org.jhotdraw8.annotation.NonNull;

import java.util.Set;

public class IndexedArrayObservableSetTest extends AbstractSetTestOld {
    @Override
    protected @NonNull <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new IndexedArrayObservableSet<>();
    }
}
