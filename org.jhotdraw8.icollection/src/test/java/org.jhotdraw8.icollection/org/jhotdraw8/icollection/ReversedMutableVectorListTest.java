package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.SequencedCollection;

public class ReversedMutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Key> newInstance() {
        return new MutableVectorList<Key>().reversed();
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new MutableVectorList<Key>().reversed();
    }
}
