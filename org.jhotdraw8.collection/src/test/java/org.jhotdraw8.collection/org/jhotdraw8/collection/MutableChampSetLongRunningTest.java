package org.jhotdraw8.collection;

import java.util.Set;

public class MutableChampSetLongRunningTest extends AbstractSetLongRunningTest {

    @Override
    Set<Long> createInstance() {
        return new MutableChampSet<>();
    }

    @Override
    long maxSize() {
        return new MutableChampSet<>().maxSize();
    }
}
