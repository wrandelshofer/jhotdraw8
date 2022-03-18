package org.jhotdraw8.collection;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexedBooleanSetTest {
    private IndexedBooleanSet newInstance(int size) {
        return new IndexedBooleanSet(size);
    }

    @Test
    public void clearAndSetUntilMarkerOverflows() {
        IndexedBooleanSet actual = newInstance(8);
        IndexedBooleanSet actual2 = newInstance(8);
        BitSet expected = new BitSet(8);

        // we intentionally clear actual2 two times here
        actual2.clear();
        actual2.clear();

        Random rng = new Random(0);

        for (int i = 0; i < 300; i++) {
            expected.clear();
            actual.clear();
            actual2.clear();

            for (int j = 0; j < 16; j++) {
                int index = rng.nextInt(8);
                boolean remove = rng.nextBoolean();


                boolean expectedChanged;
                boolean actualChanged;
                boolean actual2Changed;
                if (remove) {
                    expectedChanged = expected.get(index);
                    expected.clear(index);
                    actualChanged = actual.remove(index);
                    actual2Changed = actual2.remove(index);
                } else {
                    expectedChanged = !expected.get(index);
                    expected.set(index);
                    actualChanged = actual.add(index);
                    actual2Changed = actual2.add(index);
                }

                assertEquals(expectedChanged, actualChanged, "index=" + index + " must have changed");
                assertEquals(actualChanged, actual2Changed, "index=" + index + " must have changed");

            }
            System.out.println(expected);
            assertEquals(expected.toString(), actual.toString());
            assertEquals(expected.toString(), actual2.toString());
            assertArrayEquals(expected.toLongArray(), actual.toLongArray());
            assertArrayEquals(expected.toLongArray(), actual2.toLongArray());
            assertEquals(expected.hashCode(), actual.hashCode());
            assertEquals(expected.hashCode(), actual2.hashCode());
            assertEquals(actual, actual2);
        }
    }

    @Test
    public void resizeSet() {
        int size = 8;

        IndexedBooleanSet actual = newInstance(size);
        BitSet expected = new BitSet(size);

        Random rng = new Random(0);

        for (int i = 0; i < 300; i++) {

            boolean resize = rng.nextBoolean();
            if (resize) {
                size = 1 + rng.nextInt(128);
                BitSet temp = new BitSet(size);
                for (int j = 0; j < size; j++) {
                    temp.set(j, expected.get(j));
                }
                expected = temp;
                actual.setSize(size);

                assertEquals(expected.toString(), actual.toString());
                assertArrayEquals(expected.toLongArray(), actual.toLongArray());
                assertEquals(expected.hashCode(), actual.hashCode());
            }

            for (int j = 0; j < size * 2; j++) {
                int index = rng.nextInt(size);
                boolean remove = rng.nextBoolean();


                boolean expectedChanged;
                boolean actualChanged;
                if (remove) {
                    expectedChanged = expected.get(index);
                    expected.clear(index);
                    actualChanged = actual.remove(index);
                } else {
                    expectedChanged = !expected.get(index);
                    expected.set(index);
                    actualChanged = actual.add(index);
                }
                assertEquals(expectedChanged, actualChanged, "index=" + index + " must have changed");
            }

            assertEquals(expected.toString(), actual.toString());
            assertArrayEquals(expected.toLongArray(), actual.toLongArray());
            assertEquals(expected.hashCode(), actual.hashCode());
        }
    }
}
