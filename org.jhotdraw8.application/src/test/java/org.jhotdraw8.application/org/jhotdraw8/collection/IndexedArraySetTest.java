/* @(#)IndexedSetTest.java
 * Copyright (c) 2015 The authors and contributors of JHotDraw.
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.jhotdraw8.collection;

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
