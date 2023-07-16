package org.jhotdraw8.pcollection;

import de.sandec.jmemorybuddy.JMemoryBuddy;
import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractListTest extends AbstractSequencedCollectionTest {


    protected abstract @NonNull List<HashCollider> newListInstance();

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        Iterator<HashCollider> i = l.iterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowConcurrentModificationException(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        HashCollider valueA = data.a.iterator().next();
        l.add(valueA);
        Iterator<HashCollider> i = l.iterator();
        i.next();
        l.remove(valueA);
        assertThrows(ConcurrentModificationException.class, () -> i.next());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorNextShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<HashCollider> i = l.listIterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::next);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorNextShouldThrowNoSuchElementException2(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<HashCollider> i = l.listIterator();
        i.add(data.b.iterator().next());
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::next);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorPreviousShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<HashCollider> i = l.listIterator(0);
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::previous);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorPreviousShouldThrowNoSuchElementException2(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<HashCollider> i = l.listIterator(0);
        i.add(data.b.iterator().next());
        i.previous();
        i.remove();
        assertThrows(NoSuchElementException.class, i::previous);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorRemoveShouldThrowIllegalStateException(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        ListIterator<HashCollider> i = l.listIterator();
        i.add(data.a.iterator().next());
        assertThrows(IllegalStateException.class, i::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listShouldAffectSubList(@NonNull SetData data) throws Exception {
        List<HashCollider> l = newListInstance();
        l.add(data.a.iterator().next());
        List<HashCollider> subList = l.subList(0, 1);
        Iterator<HashCollider> subIterator = subList.iterator();
        l.set(0, data.b.iterator().next());
        HashCollider actualValue = subIterator.next();
        assertEquals(data.b.iterator().next(), actualValue);
    }

    @Test
    public void indexOfEmptyCollection() throws Exception {
        List<HashCollider> l = newListInstance();
        assertEquals(-1, l.indexOf(new HashCollider(1, -1)));
    }

    @Test
    public void lastIndexOfEmptyCollection() throws Exception {
        List<HashCollider> l = newListInstance();
        assertEquals(-1, l.lastIndexOf(new HashCollider(1, -1)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    @Disabled("test is flaky")
    public void removeOneElementShouldMakeElementCollectable(@NonNull SetData data) throws Exception {
        JMemoryBuddy.memoryTest(checker -> {
            var a = data.a.stream().map(HashCollider::clone).toList();
            var b = data.b.stream().map(HashCollider::clone).toList();
            ArrayList<HashCollider> shuffled = new ArrayList<>();
            shuffled.addAll(a);
            shuffled.addAll(b);
            Collections.shuffle(shuffled);


            var instance = newInstance();
            instance.addAll(shuffled);
            instance.remove(b.iterator().next());

            checker.setAsReferenced(instance);

            checker.assertCollectable(b.iterator().next()); // notReferenced should be collectable
        });
    }

}
