package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractListTest extends AbstractSequencedCollectionTest {
    protected abstract @NonNull List<Integer> newListInstance();

    @Test
    public void iteratorShouldThrowNoSuchElementException() {
        List<Integer> l = newListInstance();
        l.add(1);
        Iterator<Integer> i = l.iterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }

    @Test
    public void iteratorShouldThrowConcurrentModificationException() {
        List<Integer> l = newListInstance();
        l.add(1);
        Iterator<Integer> i = l.iterator();
        i.next();
        l.remove((Object) 1);
        assertThrows(ConcurrentModificationException.class, () -> i.next());
    }

    @Test
    public void listIteratorShouldThrowNoSuchElementException() {
        List<Integer> l = newListInstance();
        l.add(1);
        ListIterator<Integer> i = l.listIterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }


    @Test
    public void listIteratorShouldThrowNoSuchElementException2() {
        List<Integer> l = newListInstance();
        l.add(1);
        ListIterator<Integer> i = l.listIterator();
        i.add(7);
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }

    @Test
    public void listIteratorShouldThrowNoSuchElementException3() {
        List<Integer> l = newListInstance();
        ListIterator<Integer> i = l.listIterator();
        i.hasNext();
        i.hasNext();
        i.add(7);
        assertThrows(IllegalStateException.class, () -> i.remove());
    }

    @Test
    public void listShouldAffectSubList() {
        List<Integer> l = newListInstance();
        l.add(1);
        List<Integer> subList = l.subList(0, 1);
        Iterator<Integer> subIterator = subList.iterator();
        l.set(0, 2);
        Integer actualValue = subIterator.next();
        assertEquals(2, actualValue);
    }

}
