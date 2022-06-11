/*
 * @(#)SeqChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

public class SequencedChampSetSequencedSetTest extends AbstractSequencedSetTest {

    @Override
    protected @NonNull <T> SequencedSet<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new SequencedChampSet<>();
    }
}
