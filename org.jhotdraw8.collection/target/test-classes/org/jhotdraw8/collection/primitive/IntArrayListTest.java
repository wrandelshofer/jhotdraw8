/*
 * @(#)IntArrayListTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractIntSequencedCollectionTest;

/**
 * Tests {@link IntArrayList}.
 */
public class IntArrayListTest extends AbstractIntSequencedCollectionTest {
    @Override
    protected @NonNull IntSequencedCollection newInstance() {
        return new IntArrayList();
    }
}
