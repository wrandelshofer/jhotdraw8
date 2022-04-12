/*
 * @(#)IndexedHashSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Collection;

/**
 * Tests {@link IndexedHashObservableSet}.
 *
 * @author Werner Randelshofer
 */

public class IndexedHashSetTest extends AbstractIndexedArrayObservableSetTest {

    @Override
    protected AbstractIndexedArrayObservableSet<Character> newInstance(Collection<Character> col) {
        return new IndexedHashObservableSet<>(col);
    }

}