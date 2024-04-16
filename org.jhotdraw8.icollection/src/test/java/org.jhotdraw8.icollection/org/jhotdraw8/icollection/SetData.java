package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.facade.ReadOnlySetFacade;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * The test data.
 */
public final class SetData {
    private final @NonNull String name;
    public final @NonNull ReadOnlySet<Key> a;
    public final @NonNull ReadOnlySet<Key> b;
    public final @NonNull ReadOnlySet<Key> someAPlusSomeB;
    public final @NonNull ReadOnlySet<Key> c;

    /**
     * Creates a new instance with 3 maps of the same non-empty size.
     *
     * @param name the name of the data
     * @param a    a non-empty set that is disjoint from the other sets
     * @param b    a non-empty set that is disjoint from the other sets
     * @param c    a non-empty set that is disjoint from the other sets
     */
    SetData(String name, ReadOnlySet<Key> a,
            ReadOnlySet<Key> b,
            ReadOnlySet<Key> c) {
        this.name = name;
        this.a = a;
        this.b = b;
        this.c = c;

        LinkedHashSet<Key> someAPlusSomeB = new LinkedHashSet<>();
        ArrayList<Key> aPlusB = new ArrayList<>(a.size() + b.size());
        aPlusB.addAll(a.asSet());
        aPlusB.addAll(b.asSet());
        someAPlusSomeB.addAll(aPlusB.subList(a.size() / 2, a.size() + (b.size() + 1) / 2));
        this.someAPlusSomeB = new ReadOnlySetFacade<>(someAPlusSomeB);
    }

    public ReadOnlySet<Key> a() {
        return a;
    }

    public ReadOnlySet<Key> c() {
        return c;
    }

    public ReadOnlySet<Key> b() {
        return b;
    }

    public ReadOnlySet<Key> someAPlusSomeB() {
        return someAPlusSomeB;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


    public static @NonNull SetData newData(@NonNull String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashSet<Key> a = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Key> b = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Key> c = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyA = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            int keyB = createNewValue(rng, usedValues, bound);
            b.add(new Key(keyB, hashBitMask));
            a.add(new Key(keyA, hashBitMask));
            c.add(new Key(keyC, hashBitMask));
        }

        return new SetData(name,
                new ReadOnlySetFacade<>(a),
                new ReadOnlySetFacade<>(b),
                new ReadOnlySetFacade<>(c));
    }

    public static @NonNull SetData newNiceData(String name, int hashBitMask, int size, int bound) {
        int count = 0;
        LinkedHashSet<Key> a = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Key> b = new LinkedHashSet<>(size * 2);
        LinkedHashSet<Key> c = new LinkedHashSet<>(size * 2);
        for (int i = 0; i < size; i++) {
            int keyA = count++;
            a.add(new Key(keyA, hashBitMask));
            b.add(new Key(keyA + size, hashBitMask));
            c.add(new Key(keyA + size * 2, hashBitMask));
        }
        return new SetData(name,
                new ReadOnlySetFacade<>(a),
                new ReadOnlySetFacade<>(b),
                new ReadOnlySetFacade<>(c));
    }

    private static int createNewValue(@NonNull Random rng, @NonNull Set<Integer> usedValues, int bound) {
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
