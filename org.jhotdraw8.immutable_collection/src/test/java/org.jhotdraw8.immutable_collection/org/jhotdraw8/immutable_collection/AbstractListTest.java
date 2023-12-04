package org.jhotdraw8.immutable_collection;

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


    protected abstract @NonNull List<Key> newListInstance();

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        Iterator<Key> i = l.iterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowConcurrentModificationException(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        Key valueA = data.a.iterator().next();
        l.add(valueA);
        Iterator<Key> i = l.iterator();
        i.next();
        l.remove(valueA);
        assertThrows(ConcurrentModificationException.class, () -> i.next());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorNextShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::next);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorNextShouldThrowNoSuchElementException2(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator();
        i.add(data.b.iterator().next());
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::next);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorPreviousShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator(0);
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::previous);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorPreviousShouldThrowNoSuchElementException2(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator(0);
        i.add(data.b.iterator().next());
        i.previous();
        i.remove();
        assertThrows(NoSuchElementException.class, i::previous);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorRemoveShouldThrowIllegalStateException(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        ListIterator<Key> i = l.listIterator();
        i.add(data.a.iterator().next());
        assertThrows(IllegalStateException.class, i::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listShouldAffectSubList(@NonNull SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        List<Key> subList = l.subList(0, 1);
        Iterator<Key> subIterator = subList.iterator();
        l.set(0, data.b.iterator().next());
        Key actualValue = subIterator.next();
        assertEquals(data.b.iterator().next(), actualValue);
    }

    @Test
    public void indexOfEmptyCollection() throws Exception {
        List<Key> l = newListInstance();
        assertEquals(-1, l.indexOf(new Key(1, -1)));
    }

    @Test
    public void lastIndexOfEmptyCollection() throws Exception {
        List<Key> l = newListInstance();
        assertEquals(-1, l.lastIndexOf(new Key(1, -1)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    @Disabled("test is flaky")
    public void removeOneElementShouldMakeElementCollectable(@NonNull SetData data) throws Exception {
        JMemoryBuddy.memoryTest(checker -> {
            var a = data.a.stream().map(Key::clone).toList();
            var b = data.b.stream().map(Key::clone).toList();
            ArrayList<Key> shuffled = new ArrayList<>();
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

    @Test
    public void spliteratorShouldSupportNullKeyNullValue() throws Exception {
        List<Key> instance = newListInstance();
        assertEquals(instance.spliterator().characteristics() & Spliterator.NONNULL, 0, "spliterator should be nullable");
    }

    @Test
    public void spliteratorShouldSupportEncounterOrder() throws Exception {
        List<Key> instance = newListInstance();
        assertEquals(instance.spliterator().characteristics() & Spliterator.ORDERED, Spliterator.ORDERED, "spliterator should be ordered");
    }

    @Test
    public void listIteratorShouldSupportAddAddNextRemove() {
        List<Key> r = newListInstance();
        r.add(new Key(1));
        r.add(new Key(2));
        r.add(new Key(3));
        r.add(new Key(4));

        ListIterator<Key> i = r.listIterator();
        i.add(new Key(10));
        i.add(new Key(20));
        var next = i.next();
        assertEquals(new Key(1), next);
        i.remove();

        assertEquals(List.of(new Key(10), new Key(20), new Key(2), new Key(3), new Key(4)), r);
    }


}
