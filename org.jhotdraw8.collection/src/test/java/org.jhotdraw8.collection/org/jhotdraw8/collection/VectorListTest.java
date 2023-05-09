package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.facade.MutableListFacade;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.List;

public class VectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<HashCollider> newInstance() {
        return new MutableListFacade<HashCollider>(VectorList.<HashCollider>of());
    }

    @Override
    protected @NonNull List<HashCollider> newListInstance() {
        return new MutableListFacade<HashCollider>(VectorList.<HashCollider>of());
    }
}
