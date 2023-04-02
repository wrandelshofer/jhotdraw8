package org.jhotdraw8.collection.jmh;

import io.vavr.collection.HashMap;
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
 * ContainsFound     1000000  avgt          _   223.119          ns/op
 * ContainsNotFound  1000000  avgt          _   221.303          ns/op
 * CopyOf            1000000  avgt       330_674279.000          ns/op
 * Head              1000000  avgt          _    29.213          ns/op
 * Iterate           1000000  avgt        71_110298.376          ns/op
 * Put               1000000  avgt          _   399.898          ns/op
 * RemoveThenAdd     1000000  avgt          _   543.379          ns/op
 * Tail              1000000  avgt          _   145.562          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VavrHashMapJmh {
    @Param({"1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private HashMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = HashMap.empty();
        for (Key key : data.setA) {
            mapA = mapA.put(key, Boolean.TRUE);
        }
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : mapA.keysIterator()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public HashMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public HashMap<Key, Boolean> mPut() {
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
        return mapA.head()._1;
    }

    @Benchmark
    public HashMap<Key, Boolean> mTail() {
        return mapA.tail();
    }

    @Benchmark
    public HashMap<Key, Boolean> mCopyOf() {
        return HashMap.ofAll(data.mapA);
    }
}
