package org.jhotdraw8.icollection;


import java.util.List;
import java.util.SequencedCollection;

public class ReversedMutablePersistentVectorListTest extends AbstractListTest {
    @Override
    protected SequencedCollection<Key> newInstance() {
        return new MutableVectorList<Key>().reversed();
    }

    @Override
    protected List<Key> newListInstance() {
        return new MutableVectorList<Key>().reversed();
    }
}
