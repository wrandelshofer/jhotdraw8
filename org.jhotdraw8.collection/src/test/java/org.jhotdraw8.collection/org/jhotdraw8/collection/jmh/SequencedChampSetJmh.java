package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.champ.SequencedChampSet;
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
 *                   (mask)   (size)  Mode  Cnt    _     Score   Error  Units
 * ContainsFound        -65  1000000  avgt         _   214.381          ns/op
 * ContainsNotFound     -65  1000000  avgt         _   208.868          ns/op
 * Head                 -65  1000000  avgt         _    98.765          ns/op
 * Iterate              -65  1000000  avgt       78_730023.766          ns/op
 * RemoveThenAdd        -65  1000000  avgt         _  1144.644          ns/op
 * Tail                 -65  1000000  avgt         _   278.567          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class SequencedChampSetJmh {
    @Param({"1000000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private SequencedChampSet<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = SequencedChampSet.copyOf(data.setA);
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
    public SequencedChampSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public SequencedChampSet<Key> mTail() {
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
