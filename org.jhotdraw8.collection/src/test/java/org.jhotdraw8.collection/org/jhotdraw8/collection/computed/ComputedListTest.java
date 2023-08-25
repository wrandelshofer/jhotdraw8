package org.jhotdraw8.collection.computed;

import org.jhotdraw8.collection.AbstractReadOnlyListTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComputedListTest extends AbstractReadOnlyListTest {
    @Test
    public void shouldBeEqual() {
        List<Integer> expected = new ArrayList<>(5);
        for (int i = 5; i < 10; i++) expected.add(i);

        ComputedList<Integer> instance = new ComputedList<>(5, 10, i -> (int) i);
        assertEqualList(expected, instance);
    }

    @Test
    public void shouldBeEqualWhenDescending() {
        List<Integer> expected = new ArrayList<>(5);
        for (int i = 5; i < 10; i++) expected.add(i);
        Collections.reverse(expected);

        ComputedList<Integer> instance = new ComputedList<>(5, 10, i -> (int) i, true);
        assertEqualList(expected, instance);
    }

    @Test
    public void shouldBeEqualWhenReversed() {
        List<Integer> expected = new ArrayList<>(5);
        for (int i = 5; i < 10; i++) expected.add(i);
        Collections.reverse(expected);

        ComputedList<Integer> instance = new ComputedList<>(5, 10, i -> (int) i);
        ComputedList<Integer> actual = instance.readOnlyReversed();
        assertEqualList(expected, actual);
    }
}
