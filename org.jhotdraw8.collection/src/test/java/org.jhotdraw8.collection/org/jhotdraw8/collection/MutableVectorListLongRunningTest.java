package org.jhotdraw8.collection;

import org.junit.Ignore;

import java.util.List;

@Ignore
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
