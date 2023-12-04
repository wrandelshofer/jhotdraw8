package org.jhotdraw8.immutable_collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.sequenced.SequencedCollection;

import java.util.List;

public class ReversedMutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Key> newInstance() {
        return new MutableVectorList<Key>()._reversed();
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new MutableVectorList<Key>()._reversed();
    }
}
