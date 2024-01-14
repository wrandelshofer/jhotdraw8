package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.SequencedCollection;

public class ReversedSimpleMutableListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Key> newInstance() {
        return new SimpleMutableList<Key>().reversed();
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new SimpleMutableList<Key>().reversed();
    }
}
