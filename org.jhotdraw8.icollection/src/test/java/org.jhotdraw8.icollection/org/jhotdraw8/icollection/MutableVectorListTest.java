package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.sequenced.SequencedList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableVectorListTest extends AbstractListTest {
    @Override
    protected @NonNull SequencedList<Key> newInstance() {
        return new MutableVectorList<Key>();
    }

    @Override
    protected @NonNull List<Key> newListInstance() {
        return new MutableVectorList<Key>();
    }

    @Test
    public void reverseListIteratorShouldSupportAddAddNextRemove() {
        SequencedList<Key> r = newInstance()._reversed();
        r.add(new Key(1));
        r.add(new Key(2));
        r.add(new Key(3));
        r.add(new Key(4));

        ListIterator<Key> i = r.listIterator();
        i.add(new Key(10));
        i.add(new Key(20));
        i.next();
        i.remove();

        assertEquals(List.of(new Key(20), new Key(10), new Key(2), new Key(3), new Key(4)), r);
    }


}
