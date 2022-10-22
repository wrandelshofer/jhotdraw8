/*
 * @(#)IndexedHashObservableSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.indexedset;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractSetTestOld;

import java.util.Set;

public class IndexedHashObservableSetTest extends AbstractSetTestOld {
    @Override
    protected @NonNull <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new IndexedHashObservableSet<>();
    }
}
