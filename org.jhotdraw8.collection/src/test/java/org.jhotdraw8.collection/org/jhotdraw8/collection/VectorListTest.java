package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.facade.MutableListFacade;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.List;

public class VectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Integer> newInstance() {
        return new MutableListFacade<Integer>(VectorList.<Integer>of());
    }

    @Override
    protected @NonNull List<Integer> newListInstance() {
        return new MutableListFacade<Integer>(VectorList.<Integer>of());
    }
}
