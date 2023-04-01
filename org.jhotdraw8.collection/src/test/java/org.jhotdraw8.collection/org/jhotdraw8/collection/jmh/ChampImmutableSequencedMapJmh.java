package org.jhotdraw8.collection.jmh;

import org.jhotdraw8.collection.champ.ChampImmutableSequencedMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *                    (size)  Mode  Cnt     _     Score   Error  Units
 * ContainsFound     1000000  avgt          _   190.401          ns/op
 * ContainsNotFound  1000000  avgt          _   189.662          ns/op
 * CopyOf            1000000  avgt       316_462143.781          ns/op
 * Head              1000000  avgt        35_253418.518          ns/op
 * Iterate           1000000  avgt        81_542559.057          ns/op
 * Put               1000000  avgt          _   355.011          ns/op
 * RemoveThenAdd     1000000  avgt          _   571.975          ns/op
 * Tail              1000000  avgt        34_213526.290          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampImmutableSequencedMapJmh {
    @Param({"1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private ChampImmutableSequencedMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = ChampImmutableSequencedMap.of();
        for (Key key : data.setA) {
            mapA = mapA.put(key, Boolean.TRUE);
        }
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : mapA.readOnlyKeySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public ChampImmutableSequencedMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public ChampImmutableSequencedMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.put(key, Boolean.FALSE);
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return mapA.containsKey(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return mapA.containsKey(key);
    }

    @Benchmark
    public Key mHead() {
        return mapA.iterator().next().getKey();
    }

    @Benchmark
    public ChampImmutableSequencedMap<Key, Boolean> mTail() {
        return mapA.remove(mapA.iterator().next().getKey());
    }

    @Benchmark
    public ChampImmutableSequencedMap<Key, Boolean> mCopyOf() {
        return ChampImmutableSequencedMap.<Key, Boolean>copyOf(data.mapA);
    }
}
