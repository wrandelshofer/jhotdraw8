package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.impl.facade.MutableListFacade;
import org.jhotdraw8.pcollection.sequenced.SequencedCollection;

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
