package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlyMapFacade;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.*;

/**
 * The test data.
 */
public final class MapData {
    private final String name;
    public final ReadOnlyMap<Key, Key> a;
    public final ReadOnlyMap<Key, Key> aWithDifferentValues;
    public final ReadOnlyMap<Key, Key> c;
    private final ReadOnlyMap<Key, Key> someAPlusSomeB;
    public final ReadOnlyMap<Key, Key> b;

    /**
     * Creates a new instance with 3 maps of the same non-empty size.
     *
     * @param name                 the name of the data
     * @param a                    a non-empty map, all values are distinct
     * @param aWithDifferentValues a map with identical keys but different values from a,
     *                             all values are distinct from the values in a
     *                             and from other values in b
     * @param c                    a map with different keys and values from a,
     *                             all values are distinct from the values in a.
     */
    MapData(String name, ReadOnlyMap<Key, Key> a,
            ReadOnlyMap<Key, Key> aWithDifferentValues,
            ReadOnlyMap<Key, Key> b,
            ReadOnlyMap<Key, Key> c) {
        this.name = name;
        this.a = a;
        this.aWithDifferentValues = aWithDifferentValues;
        this.c = c;
        this.b = b;

        LinkedHashMap<Key, Key> someAPlusSomeB = new LinkedHashMap<>();
        ArrayList<Map.Entry<Key, Key>> aPlusB = new ArrayList<>(a.size() + b.size());
        aPlusB.addAll(a.readOnlyEntrySet().asSet());
        aPlusB.addAll(b.readOnlyEntrySet().asSet());
        aPlusB.subList(a.size() / 2, a.size() + (b.size() + 1) / 2)
                .iterator().forEachRemaining(e -> someAPlusSomeB.put(e.getKey(), e.getValue()));
        this.someAPlusSomeB = new ReadOnlyMapFacade<>(someAPlusSomeB);

    }

    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    public ReadOnlyMap<Key, Key> a() {
        return a;
    }

    public ReadOnlyMap<Key, Key> aWithDifferentValues() {
        return aWithDifferentValues;
    }

    public ReadOnlyMap<Key, Key> b() {
        return b;
    }

    public ReadOnlyMap<Key, Key> someAPlusSomeB() {
        return someAPlusSomeB;
    }

    public ReadOnlyMap<Key, Key> c() {
        return c;
    }


    public static MapData newData(String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashMap<Key, Key> a = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Key> b = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Key> aWithDifferentValues = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Key> c = new LinkedHashMap<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyA = createNewValue(rng, usedValues, bound);
            int keyB = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            int valueA = createNewValue(rng, usedValues, bound);
            int valueDifferentFromA = createNewValue(rng, usedValues, bound);
            int valueB = createNewValue(rng, usedValues, bound);
            int valueC = createNewValue(rng, usedValues, bound);
            a.put(new Key(keyA, hashBitMask), new Key(valueA, hashBitMask));
            aWithDifferentValues.put(new Key(keyA, hashBitMask), new Key(valueDifferentFromA, hashBitMask));
            b.put(new Key(keyB, hashBitMask), new Key(valueB, hashBitMask));
            c.put(new Key(keyC, hashBitMask), new Key(valueC, hashBitMask));
        }
        return new MapData(name,
                new ReadOnlyMapFacade<>(a),
                new ReadOnlyMapFacade<>(aWithDifferentValues),
                new ReadOnlyMapFacade<>(b),
                new ReadOnlyMapFacade<>(c));
    }

    private static int createNewValue(Random rng, Set<Integer> usedValues, int bound) {
        int value;
        int count = 0;
        do {
            value = rng.nextInt(bound);
            count++;
            if (count >= bound) {
                throw new RuntimeException("error in rng");
            }
        } while (!usedValues.add(value));
        return value;
    }
}
