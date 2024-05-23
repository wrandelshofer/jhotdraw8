package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.MutableListFacade;

import java.util.List;
import java.util.SequencedCollection;

public class ReversedVectorListTest extends AbstractListTest {
    @Override
    protected SequencedCollection<Key> newInstance() {
        return new MutableListFacade<Key>(VectorList.<Key>of()).reversed();
    }

    @Override
    protected List<Key> newListInstance() {
        return new MutableListFacade<Key>(VectorList.<Key>of()).reversed();
    }
}
