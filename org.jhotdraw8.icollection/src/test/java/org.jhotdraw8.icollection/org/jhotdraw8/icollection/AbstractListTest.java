package org.jhotdraw8.icollection;

import de.sandec.jmemorybuddy.JMemoryBuddy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractListTest extends AbstractSequencedCollectionTest {


    protected abstract List<Key> newListInstance();

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowNoSuchElementException(SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        Iterator<Key> i = l.iterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, () -> i.next());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorShouldThrowConcurrentModificationException(SetData data) throws Exception {
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
    public void listIteratorNextShouldThrowNoSuchElementException(SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator();
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::next);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorNextShouldThrowNoSuchElementException2(SetData data) throws Exception {
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
    public void listIteratorPreviousShouldThrowNoSuchElementException(SetData data) throws Exception {
        List<Key> l = newListInstance();
        l.add(data.a.iterator().next());
        ListIterator<Key> i = l.listIterator(0);
        i.next();
        i.remove();
        assertThrows(NoSuchElementException.class, i::previous);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listIteratorPreviousShouldThrowNoSuchElementException2(SetData data) throws Exception {
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
    public void listIteratorRemoveShouldThrowIllegalStateException(SetData data) throws Exception {
        List<Key> l = newListInstance();
        ListIterator<Key> i = l.listIterator();
        i.add(data.a.iterator().next());
        assertThrows(IllegalStateException.class, i::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void listShouldAffectSubList(SetData data) throws Exception {
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
    public void removeOneElementShouldMakeElementCollectable(SetData data) throws Exception {
        JMemoryBuddy.memoryTest(checker -> {
            var a = data.a.stream().map(Key::clone).toList();
            var b = data.b.stream().map(Key::clone).toList();
            ArrayList<Key> shuffled = new ArrayList<>();
            shuffled.addAll(a);
            shuffled.addAll(b);
            Collections.shuffle(shuffled);


            var instance = newInstance();
            instance.addAll(shuffled);
            instance.remove(b.getFirst());

            checker.setAsReferenced(instance);

            checker.assertCollectable(b.getFirst()); // notReferenced should be collectable
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
        List<Key> initialList = List.of(new Key(1), new Key(2), new Key(3), new Key(4));

        List<Key> expected = new ArrayList<>();
        expected.addAll(initialList);
        ListIterator<Key> j = expected.listIterator();
        j.add(new Key(10));
        j.add(new Key(20));
        var next = j.next();
        assertEquals(new Key(1), next);
        j.remove();

        List<Key> actual = newListInstance();
        actual.addAll(initialList);
        ListIterator<Key> i = actual.listIterator();
        i.add(new Key(10));
        i.add(new Key(20));
        next = i.next();
        assertEquals(new Key(1), next);
        i.remove();

        assertEquals(expected, actual);
    }

    @Test
    public void addShouldSupportConsecutiveInsertionInTheMiddleWithIncreasingIndex() {
        List<Key> actual = newListInstance();
        actual.addAll(List.of(new Key(1), new Key(2), new Key(7), new Key(8)));

        actual.add(2, new Key(3));
        assertEquals(List.of(new Key(1), new Key(2), new Key(3), new Key(7), new Key(8)), actual);

        actual.add(3, new Key(4));
        assertEquals(List.of(new Key(1), new Key(2), new Key(3), new Key(4), new Key(7), new Key(8)), actual);

        actual.add(4, new Key(5));
        assertEquals(List.of(new Key(1), new Key(2), new Key(3), new Key(4), new Key(5), new Key(7), new Key(8)), actual);

        actual.add(5, new Key(6));
        assertEquals(List.of(new Key(1), new Key(2), new Key(3), new Key(4), new Key(5), new Key(6), new Key(7), new Key(8)), actual);
    }

    @Test
    public void removeShouldSupportConsecutiveRemovalInTheMiddleWithSameIndex() {
        List<Key> actual = newListInstance();
        actual.addAll(List.of(new Key(1), new Key(2), new Key(3), new Key(4), new Key(5), new Key(6), new Key(7), new Key(8)));

        actual.remove(2);
        assertEquals(List.of(new Key(1), new Key(2), new Key(4), new Key(5), new Key(6), new Key(7), new Key(8)), actual);

        actual.remove(2);
        assertEquals(List.of(new Key(1), new Key(2), new Key(5), new Key(6), new Key(7), new Key(8)), actual);

        actual.remove(2);
        assertEquals(List.of(new Key(1), new Key(2), new Key(6), new Key(7), new Key(8)), actual);

        actual.remove(2);
        assertEquals(List.of(new Key(1), new Key(2), new Key(7), new Key(8)), actual);
    }


}
