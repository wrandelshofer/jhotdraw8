package org.jhotdraw8.collection;

import java.util.List;

public class MutableVectorListLongRunningTest extends AbstractListLongRunningTest {

    @Override
    List<Long> createInstance() {
        return new MutableVectorList<>();
    }

    @Override
    long maxSize() {
        return new MutableVectorList<>().maxSize();
    }
}
