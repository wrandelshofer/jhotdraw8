package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.collection.VectorList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ZipperTest {
    @Test
    public void shouldZipList() {
        VectorList<String> actual = VectorList.of("a", "b", "c");
        actual = actual.transformed().by(ZipTransformer.zipList((a, b) -> a + b));
        VectorList<String> expected = VectorList.of("ab", "c");
        assertEquals(expected, actual);
    }

    @Test
    public void shouldZipListInReverse() {
        VectorList<String> actual = VectorList.of("a", "b", "c");
        actual = actual.transformed().by(ZipTransformer.zipListInReverse((a, b) -> a + b));
        VectorList<String> expected = VectorList.of("a", "bc");
        assertEquals(expected, actual);
    }

    @Test
    public void shouldZipListInReverseUntilSizeIsOne() {
        VectorList<String> actual = VectorList.of("a", "b", "c", "d", "e", "f", "g");
        while (actual.size() > 1) {
            actual = actual.transformed().by(ZipTransformer.zipListInReverse((a, b) -> a + b));
        }
        VectorList<String> expected = VectorList.of("abcdefg");
        assertEquals(expected, actual);
    }
}
