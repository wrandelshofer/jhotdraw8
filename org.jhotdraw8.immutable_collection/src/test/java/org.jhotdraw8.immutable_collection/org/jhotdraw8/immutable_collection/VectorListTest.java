package org.jhotdraw8.immutable_collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.facade.MutableListFacade;
import org.jhotdraw8.immutable_collection.sequenced.SequencedCollection;

import java.util.List;

public class VectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Key> newInstance() {
        return new MutableListFacade<Key>(VectorList.<Key>of());
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new MutableListFacade<Key>(VectorList.<Key>of());
    }
}
