package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableMapFacade;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The test data.
 */
public final class MapData {
    private final String name;
    public final ReadableMap<Key, Value> a;
    public final ReadableMap<Key, Value> aWithDifferentValues;
    public final ReadableMap<Key, Value> c;
    private final ReadableMap<Key, Value> someAPlusSomeB;
    public final ReadableMap<Key, Value> b;

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
    MapData(String name, ReadableMap<Key, Value> a,
            ReadableMap<Key, Value> aWithDifferentValues,
            ReadableMap<Key, Value> b,
            ReadableMap<Key, Value> c) {
        this.name = name;
        this.a = a;
        this.aWithDifferentValues = aWithDifferentValues;
        this.c = c;
        this.b = b;

        LinkedHashMap<Key, Value> someAPlusSomeB = new LinkedHashMap<>();
        ArrayList<Map.Entry<Key, Value>> aPlusB = new ArrayList<>(a.size() + b.size());
        aPlusB.addAll(a.readableEntrySet().asSet());
        aPlusB.addAll(b.readableEntrySet().asSet());
        aPlusB.subList(a.size() / 2, a.size() + (b.size() + 1) / 2)
                .iterator().forEachRemaining(e -> someAPlusSomeB.put(e.getKey(), e.getValue()));
        this.someAPlusSomeB = new ReadableMapFacade<>(someAPlusSomeB);

    }

    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    public ReadableMap<Key, Value> a() {
        return a;
    }

    public ReadableMap<Key, Value> aWithDifferentValues() {
        return aWithDifferentValues;
    }

    public ReadableMap<Key, Value> b() {
        return b;
    }

    public ReadableMap<Key, Value> someAPlusSomeB() {
        return someAPlusSomeB;
    }

    public ReadableMap<Key, Value> c() {
        return c;
    }


    public static MapData newData(String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashMap<Key, Value> a = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Value> b = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Value> aWithDifferentValues = new LinkedHashMap<>(size * 2);
        LinkedHashMap<Key, Value> c = new LinkedHashMap<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyA = createNewValue(rng, usedValues, bound);
            int keyB = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            int valueA = createNewValue(rng, usedValues, bound);
            int valueDifferentFromA = createNewValue(rng, usedValues, bound);
            int valueB = createNewValue(rng, usedValues, bound);
            int valueC = createNewValue(rng, usedValues, bound);
            a.put(new Key(keyA, hashBitMask), new Value(valueA, hashBitMask));
            aWithDifferentValues.put(new Key(keyA, hashBitMask), new Value(valueDifferentFromA, hashBitMask));
            b.put(new Key(keyB, hashBitMask), new Value(valueB, hashBitMask));
            c.put(new Key(keyC, hashBitMask), new Value(valueC, hashBitMask));
        }
        return new MapData(name,
                new ReadableMapFacade<>(a),
                new ReadableMapFacade<>(aWithDifferentValues),
                new ReadableMapFacade<>(b),
                new ReadableMapFacade<>(c));
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
