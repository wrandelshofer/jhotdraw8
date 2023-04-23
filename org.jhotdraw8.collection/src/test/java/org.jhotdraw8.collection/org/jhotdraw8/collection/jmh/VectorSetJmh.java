package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.VectorSet;
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
 * Benchmark                       (mask)   (size)  Mode  Cnt         Score   Error  Units
 * VectorSetJmh.mAdd                  -65  1000000  avgt            214.748          ns/op
 * VectorSetJmh.mContainsFound        -65  1000000  avgt            196.670          ns/op
 * VectorSetJmh.mContainsNotFound     -65  1000000  avgt            194.472          ns/op
 * VectorSetJmh.mHead                 -65  1000000  avgt             12.710          ns/op
 * VectorSetJmh.mIterate              -65  1000000  avgt       65039254.825          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65  1000000  avgt            928.944          ns/op
 * VectorSetJmh.mTail                 -65  1000000  avgt            145.287          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorSetJmh {
    @Param({"1000000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private VectorSet<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = VectorSet.copyOf(data.setA);
    }

    @Benchmark
    public VectorSet<Key> mCopyOf() {
        return VectorSet.copyOf(data.setA);
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
    public VectorSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Object mAdd() {
        Key key = data.nextKeyInB();
        return setA.add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public VectorSet<Key> mTail() {
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
