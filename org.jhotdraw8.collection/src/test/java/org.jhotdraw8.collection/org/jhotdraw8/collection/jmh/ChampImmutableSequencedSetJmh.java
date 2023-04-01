package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.champ.ChampImmutableSequencedSet;
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
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *                   (mask)   (size)  Mode  Cnt         Score   Error  Units
 * ContainsFound        -65  1000000  avgt    2       204.253          ns/op
 * ContainsNotFound     -65  1000000  avgt    2       204.079          ns/op
 * Head                 -65  1000000  avgt    2  30313036.380          ns/op
 * Iterate              -65  1000000  avgt    2  60016917.215          ns/op
 * RemoveThenAdd        -65  1000000  avgt    2       625.272          ns/op
 * Tail                 -65  1000000  avgt    2  18838851.234          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampImmutableSequencedSetJmh {
    @Param({"1000000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private ChampImmutableSequencedSet<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ChampImmutableSequencedSet.copyOf(data.setA);
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public ChampImmutableSequencedSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public ChampImmutableSequencedSet<Key> mTail() {
        return setA.remove(setA.getFirst());
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return setA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return setA.contains(key);
    }
}
