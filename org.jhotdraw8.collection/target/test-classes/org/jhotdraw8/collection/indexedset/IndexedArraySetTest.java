/*
 * @(#)IndexedArraySetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.indexedset;

import org.jhotdraw8.collection.AbstractIndexedArrayObservableSetTest;

import java.util.Collection;

/**
 * Tests {@link IndexedArrayObservableSet}.
 *
 * @author Werner Randelshofer
 */
public class IndexedArraySetTest extends AbstractIndexedArrayObservableSetTest {


    @Override
    protected AbstractIndexedArrayObservableSet<Character> newInstance(Collection<Character> col) {
        return new IndexedArrayObservableSet<>(col);
    }
}
