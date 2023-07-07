package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.SequencedCollection;

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
