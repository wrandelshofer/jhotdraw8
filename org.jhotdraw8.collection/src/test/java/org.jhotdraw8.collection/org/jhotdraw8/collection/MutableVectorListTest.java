package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.List;

public class MutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Integer> newInstance() {
        return new MutableVectorList<Integer>();
    }

    @Override
    protected @NonNull List<Integer> newListInstance() {
        return new MutableVectorList<Integer>();
    }
}
