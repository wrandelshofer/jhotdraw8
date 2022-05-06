/*
 * @(#)TrieMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.ToIntFunction;

public class ChampMapTest extends AbstractMapTest {
    /**
     * Orders the trie by hash-code with bit partition size 5.
     * <p>
     * If the trie has this bit partition size, and the iterator of the set would
     * traverse the trie in pre-order sequence,
     * then iteration sequence would be ordered by the unsigned value of the
     * hash code.
     */
    private final static ToIntFunction<Object> hashFunction5 = o -> {
        long h = Objects.hashCode(o);
        long lsb = h & 0b11;
        h = h >>> 2;
        h = (h & 0b11111000_00111110000011111000001111100000L) >>> 5 | (h & 0b00000111_11000001111100000111110000011111L) << 5;
        h = (h & 0b11111111_11000000000011111111110000000000L) >>> 10 | (h & 0b00000000_00111111111100000000001111111111L) << 10;
        h = (h & 0b11111111_11111111111100000000000000000000L) >>> 20 | (h & 0b00000000_00000000000011111111111111111111L) << 20;
        return (int) ((h >>> 10) | (lsb << 30));
    };
    private final static ToIntFunction<Object> invHashFunction5 = o -> {
        long h = Objects.hashCode(o);
        long msb = h & 0b11000000_00000000L;
        h = (h & 0b11111000_00111110000011111000001111100000L) >>> 5 | (h & 0b00000111_11000001111100000111110000011111L) << 5;
        h = (h & 0b11111111_11000000000011111111110000000000L) >>> 10 | (h & 0b00000000_00111111111100000000001111111111L) << 10;
        h = (h & 0b11111111_11111111111100000000000000000000L) >>> 20 | (h & 0b00000000_00000000000011111111111111111111L) << 20;
        return (int) ((h >>> 8) | msb);
    };


    @Override
    protected @NonNull Map<HashCollider, HashCollider> of() {
        return new ChampMap<>();
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Map<HashCollider, HashCollider> map) {
        return new ChampMap<>(map);
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Collection<Map.Entry<HashCollider, HashCollider>> map) {
        return new ChampMap<>(map);
    }

    @Test
    public void testDumpStructure() {
        ChampMap<Integer, String> instance = new ChampMap<>();
        Random rng = new Random(0);
        for (int i = 0; i < 5; i++) {
            int key = rng.nextInt(10_000);
            char value = (char) (rng.nextInt(26) + 'a');
            instance.put(key, Character.toString(value));
        }

        System.out.println(instance.dump());

    }

    @Test
    public void testCopyAddAll() {
        ChampMap<Integer, String> instance = new ChampMap<>();
        //  Random rng=new Random(0);
        //  for (int i=0;i<30;i++){
        //      int key = rng.nextInt(10_000);
        //      char value = (char)(rng.nextInt(26) + 'a');
        //      instance.put(key,Character.toString(value));
        //  }

        instance.put(0b11000000000000000000000000000000, "a");
        instance.put(0b00111110000000000000000000000000, "b");
        instance.put(0b00000001111100000000000000000000, "c");
        instance.put(0b00000000000011111000000000000000, "d");
        instance.put(0b00000000000000000111110000000000, "e");
        instance.put(0b00000000000000000000001111100000, "f");
        instance.put(0b00000000000000000000000000011111, "g");


        ArrayList<Map.Entry<Integer, String>> list = new ArrayList<>(instance.entrySet());
        list.sort(Comparator.comparingLong(e -> invHashFunction5.applyAsInt(e.getKey()) & 0xffffffffL));
        for (Map.Entry<Integer, String> entry : list) {
            Integer key = entry.getKey();
            System.out.println("expected: " + entry.getValue() + " " + Integer.toBinaryString(key) + " " + Integer.toBinaryString(invHashFunction5.applyAsInt(key)));
        }

        for (Map.Entry<Integer, String> entry : instance.entrySet()) {
            Integer key = entry.getKey();
            System.out.println("actual  : " + entry.getValue() + " " + Integer.toBinaryString(key) + " " + Integer.toBinaryString(invHashFunction5.applyAsInt(key)));
        }


        System.out.println(instance.dump());

    }
}
