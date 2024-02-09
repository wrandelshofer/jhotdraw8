package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedCollection<Key> newInstance() {
        return new MutableVectorList<Key>();
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new MutableVectorList<Key>();
    }

    @Test
    public void reverseListIteratorShouldSupportAddAddNextRemove() {
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
