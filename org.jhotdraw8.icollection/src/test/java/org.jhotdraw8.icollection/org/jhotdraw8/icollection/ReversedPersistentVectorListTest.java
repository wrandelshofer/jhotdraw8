package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.MutableListFacade;

import java.util.List;
import java.util.SequencedCollection;

public class ReversedPersistentVectorListTest extends AbstractListTest {
    @Override
    protected SequencedCollection<Key> newInstance() {
        return new MutableListFacade<Key>(PersistentVectorList.<Key>of()).reversed();
    }

    @Override
    protected List<Key> newListInstance() {
        return new MutableListFacade<Key>(PersistentVectorList.<Key>of()).reversed();
    }
}
