package org.jhotdraw8.icollection.jmh;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/// This class provides collections that can be used in JMH benchmarks.
/// Collections 'a' and 'b' are disjoint.
/// Collection 'c' contains 50% of 'a' and 50% of 'b'.
@SuppressWarnings("JmhInspections")
public class BenchmarkData {
    /// List 'a'.
    ///
    /// The elements have been shuffled, so that they
    /// are not in contiguous memory addresses.
    public final List<Key> listA;
    public final Key[] arrayA;
    private final List<Integer> indicesA;
    private final List<Integer> indicesC;
    /// Set 'a'.
    public final Set<Key> setA;
    /// Set 'b'.
    public final Set<Key> setB;
    /// Set 'c'.
    public final Set<Key> setC;
    /// Map 'a'.
    public final Map<Key, Boolean> mapA;
    public final Map<Key, Boolean> mapC;
    /// List 'b'.
    ///
    /// The elements have been shuffled, so that they
    /// are not in contiguous memory addresses.
    public final List<Key> listB;
    /// List 'c'.
    ///
    /// The elements have been shuffled, so that they
    /// are not in contiguous memory addresses.
    public final List<Key> listC;


    private int index;
    private final int size;

    public BenchmarkData(int size, int mask) {
        this.size = size;
        Random rng = new Random(0);
        int initialCapacity = (int) Math.min(Integer.MAX_VALUE, size * 2L);
        Set<Integer> preventDuplicates = new HashSet<>(initialCapacity);
        ArrayList<Key> keysInA = new ArrayList<>(size);
        mapA = new HashMap<>(initialCapacity);
        ArrayList<Key> keysNotInA = new ArrayList<>(size);
        Map<Key, Integer> indexMap = new HashMap<>(initialCapacity);
        for (int i = 0; i < size; i++) {
            Key key = createKey(rng, preventDuplicates, mask);
            keysInA.add(key);
            mapA.put(key, Boolean.TRUE);
            indexMap.put(key, i);
            keysNotInA.add(createKey(rng, preventDuplicates, mask));
        }
        ArrayList<Key> keys50PercentInA = new ArrayList<>(size);
        keys50PercentInA.addAll(keysInA.subList(0, size / 2));
        keys50PercentInA.addAll(keysNotInA.subList(size / 2, size));
        setA = new HashSet<>(keysInA);
        setB = new HashSet<>(keysNotInA);
        setC = new HashSet<>(keys50PercentInA);
        mapC = new HashMap<>();
        keys50PercentInA.forEach(k -> mapC.put(k, Boolean.FALSE));
        Collections.shuffle(keysInA);
        Collections.shuffle(keysNotInA);
        Collections.shuffle(keys50PercentInA);
        this.listA = Collections.unmodifiableList(keysInA);
        this.arrayA = keysInA.toArray(new Key[0]);
        this.listB = Collections.unmodifiableList(keysNotInA);
        this.listC = Collections.unmodifiableList(keys50PercentInA);
        indicesA = new ArrayList<>(keysInA.size());

        for (var k : keysInA) {
            indicesA.add(indexMap.get(k));
        }
        indicesC = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indicesC.add(i);
        }
        Collections.shuffle(indicesC);
    }

    private Key createKey(Random rng, Set<Integer> preventDuplicates, int mask) {
        int candidate = rng.nextInt();
        while (!preventDuplicates.add(candidate)) {
            candidate = rng.nextInt();
        }
        return new Key(candidate, mask);
    }

    public Key nextKeyInA() {
        index = index < size - 1 ? index + 1 : 0;
        return listA.get(index);
    }

    public int nextIndexInA() {
        index = index < size - 1 ? index + 1 : 0;
        return indicesA.get(index);
    }

    public Key nextKeyInB() {
        index = index < size - 1 ? index + 1 : 0;
        return listA.get(index);
    }

    public Key nextKeyInC() {
        index = index < size - 1 ? index + 1 : 0;
        return listC.get(index);
    }
}
