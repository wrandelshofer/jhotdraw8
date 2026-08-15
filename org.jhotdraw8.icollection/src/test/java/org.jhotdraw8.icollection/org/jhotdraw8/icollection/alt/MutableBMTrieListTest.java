package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractListTest;
import org.jhotdraw8.icollection.Key;
import org.jhotdraw8.icollection.SetData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableBMTrieListTest extends AbstractListTest {
    @Override
    protected SequencedCollection<Key> newInstance() {
        return new MutableBMTrieList<Key>();
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

    @Override
    protected List<Key> newListInstance() {
        return new MutableBMTrieList<Key>();
    }

    @Test
    public void reversedListIteratorShouldSupportAddingAtAddingNextRemoving() {
        List<Key> expected = new ArrayList<>();
        expected.add(new Key(1));
        expected.add(new Key(2));
        expected.add(new Key(3));
        expected.add(new Key(4));

        ListIterator<Key> i = expected.listIterator();
        i.add(new Key(10));
        i.add(new Key(20));
        i.next();
        i.remove();

        assertEquals(List.of(new Key(10), new Key(20), new Key(2), new Key(3), new Key(4)), expected);

        List<Key> actual = newListInstance().reversed();
        actual.add(new Key(1));
        actual.add(new Key(2));
        actual.add(new Key(3));
        actual.add(new Key(4));

        i = actual.listIterator();
        i.add(new Key(10));
        i.add(new Key(20));
        i.next();
        i.remove();

        assertEquals(List.of(new Key(10), new Key(20), new Key(2), new Key(3), new Key(4)), actual);
    }


}
