package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.sequenced.SequencedCollection;

import java.util.List;

public class MutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<HashCollider> newInstance() {
        return new MutableVectorList<HashCollider>();
    }

    @Override
    protected @NonNull List<HashCollider> newListInstance() {
        return new MutableVectorList<HashCollider>();
    }
}
