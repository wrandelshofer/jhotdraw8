package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.facade.MutableListFacade;
import org.jhotdraw8.pcollection.sequenced.SequencedCollection;

import java.util.List;

public class ReversedVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<HashCollider> newInstance() {
        return new MutableListFacade<HashCollider>(VectorList.<HashCollider>of())._reversed();
    }

    @Override
    protected @NonNull List<HashCollider> newListInstance() {
        return new MutableListFacade<HashCollider>(VectorList.<HashCollider>of())._reversed();
    }
}
