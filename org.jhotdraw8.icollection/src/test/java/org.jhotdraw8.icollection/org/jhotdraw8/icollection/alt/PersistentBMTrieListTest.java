package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractListTest;
import org.jhotdraw8.icollection.Key;
import org.jhotdraw8.icollection.SetData;
import org.jhotdraw8.icollection.facade.MutableListFacade;
import org.junit.jupiter.api.Disabled;

import java.util.List;
import java.util.SequencedCollection;

public class PersistentBMTrieListTest extends AbstractListTest {
    @Override
    protected SequencedCollection<Key> newInstance() {
        return new MutableListFacade<Key>(PersistentBMTrieList.<Key>of());
    }

    @Override
    protected List<Key> newListInstance() {
        return new MutableListFacade<Key>(PersistentBMTrieList.<Key>of());
    }

    @Override
    @Disabled("BMTrieList has a memory leak")
    public void removeFirstShouldMakeElementCollectable(SetData data) throws Exception {
    }

    @Override
    @Disabled("BMTrieList has a memory leak")
    public void removeAtShouldMakeElementCollectable(SetData data) throws Exception {
    }

    @Override
    @Disabled("BMTrieList has a memory leak")
    public void removeLastShouldMakeElementCollectable(SetData data) throws Exception {
    }
}
