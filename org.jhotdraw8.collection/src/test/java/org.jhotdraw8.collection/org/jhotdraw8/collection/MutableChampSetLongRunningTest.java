package org.jhotdraw8.collection;

import org.junit.Ignore;

import java.util.Set;

@Ignore
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
