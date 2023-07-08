package org.jhotdraw8.pcollection;

import org.jhotdraw8.pcollection.facade.ReadOnlyMapFacade;
import org.jhotdraw8.pcollection.readonly.ReadOnlyMap;

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
    public final ReadOnlyMap<HashCollider, HashCollider> a;
    public final ReadOnlyMap<HashCollider, HashCollider> aWithDifferentValues;
    public final ReadOnlyMap<HashCollider, HashCollider> c;
    private final ReadOnlyMap<HashCollider, HashCollider> someAPlusSomeB;
    public final ReadOnlyMap<HashCollider, HashCollider> b;

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
    MapData(String name, ReadOnlyMap<HashCollider, HashCollider> a,
            ReadOnlyMap<HashCollider, HashCollider> aWithDifferentValues,
            ReadOnlyMap<HashCollider, HashCollider> b,
            ReadOnlyMap<HashCollider, HashCollider> c) {
        this.name = name;
        this.a = a;
        this.aWithDifferentValues = aWithDifferentValues;
        this.c = c;
        this.b = b;

        LinkedHashMap<HashCollider, HashCollider> someAPlusSomeB = new LinkedHashMap<>();
        ArrayList<Map.Entry<HashCollider, HashCollider>> aPlusB = new ArrayList<>(a.size() + b.size());
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

    public ReadOnlyMap<HashCollider, HashCollider> a() {
        return a;
    }

    public ReadOnlyMap<HashCollider, HashCollider> aWithDifferentValues() {
        return aWithDifferentValues;
    }

    public ReadOnlyMap<HashCollider, HashCollider> b() {
        return b;
    }

    public ReadOnlyMap<HashCollider, HashCollider> someAPlusSomeB() {
        return someAPlusSomeB;
    }

    public ReadOnlyMap<HashCollider, HashCollider> c() {
        return c;
    }


    public static MapData newData(String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashMap<HashCollider, HashCollider> a = new LinkedHashMap<>(size * 2);
        LinkedHashMap<HashCollider, HashCollider> b = new LinkedHashMap<>(size * 2);
        LinkedHashMap<HashCollider, HashCollider> aWithDifferentValues = new LinkedHashMap<>(size * 2);
        LinkedHashMap<HashCollider, HashCollider> c = new LinkedHashMap<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyA = createNewValue(rng, usedValues, bound);
            int keyB = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            int valueA = createNewValue(rng, usedValues, bound);
            int valueDifferentFromA = createNewValue(rng, usedValues, bound);
            int valueB = createNewValue(rng, usedValues, bound);
            int valueC = createNewValue(rng, usedValues, bound);
            a.put(new HashCollider(keyA, hashBitMask), new HashCollider(valueA, hashBitMask));
            aWithDifferentValues.put(new HashCollider(keyA, hashBitMask), new HashCollider(valueDifferentFromA, hashBitMask));
            b.put(new HashCollider(keyB, hashBitMask), new HashCollider(valueB, hashBitMask));
            c.put(new HashCollider(keyC, hashBitMask), new HashCollider(valueC, hashBitMask));
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
