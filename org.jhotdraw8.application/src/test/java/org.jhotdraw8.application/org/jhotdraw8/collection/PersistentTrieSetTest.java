package org.jhotdraw8.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PersistentTrieSetTest {
    @Test
    void testWithAdd() {
        PersistentSet<Integer> set = PersistentTrieSet.of();

        PersistentSet<Integer> one = set.withAdd(1);
        assertEquals(Set.of(1), one);

        PersistentSet<Integer> two = one.withAdd(2);
        assertEquals(Set.of(1, 2), two);
    }


    @Test
    void testWithAddAndWithRemoveAndHashCollision() {
        PersistentTrieSet<Long> actual = PersistentTrieSet.of();
        LinkedHashSet<Long> expected = new LinkedHashSet<>();

        int bound = 1079;
        Random rng = new Random(0);
        for (int i = 0; i < 2048; i++) {
            long value = rng.nextInt(bound);

            assertEquals(expected.contains(value), actual.contains(value));

            switch (rng.nextInt(12)) {
            case 0:
                actual = actual.withAdd(value);
                expected.add(value);
                long collision = value << 32;
                if (value != 0) {
                    assertNotEquals(value, collision);
                }
                assertEquals(Long.valueOf(value).hashCode(), Long.valueOf(collision).hashCode());
                actual = actual.withAdd(collision);
                expected.add(collision);
                break;
            case 1:
                actual = actual.withAddAll(List.of(value));
                expected.add(value);
                break;
            case 2:
                actual = actual.withRemove(value);
                expected.remove(value);
                break;
            case 3:
                actual = actual.withRemoveAll(List.of(value));
                expected.remove(value);
                break;
            case 4:
                actual = actual.withRetainAll(List.of(value));
                expected.retainAll(List.of(value));
                break;
            case 5: {
                actual = actual.withAddAll(PersistentTrieSet.of(value));
                expected.addAll(Arrays.asList(value));
                break;
            }
            case 6: {
                Long[] integers = {value, (long) rng.nextInt(bound)};
                actual = actual.withAddAll(PersistentTrieSet.of(integers[0], integers[1]));
                expected.addAll(Arrays.asList(integers));
                break;
            }
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                // The set must grow, so that it consists of more than just
                // one node.
            {
                Long[] integers = {value, (long) rng.nextInt(bound), (long) rng.nextInt(bound), (long) rng.nextInt(bound)};
                actual = actual.withAddAll(PersistentTrieSet.of(integers));
                expected.addAll(Arrays.asList(integers));
                break;
            }
            default:
                fail();
            }
            //noinspection SimplifiableAssertion,EqualsBetweenInconvertibleTypes
            assertTrue(actual.equals(expected));
            assertEquals(expected, actual.asSet());
            assertEquals(expected.hashCode(), actual.hashCode());
            assertEquals(expected.size(), actual.size());
            assertEquals(expected.contains(value), actual.contains(value));
            assertEquals(expected.containsAll(List.of(value)), actual.containsAll(List.of(value)));
            assertEquals(expected.isEmpty(), actual.isEmpty());
            System.out.println(actual.toString());//tests to String
        }
    }
}