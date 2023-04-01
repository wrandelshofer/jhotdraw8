package org.jhotdraw8.collection.jmh;

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
import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.immutable.Map;
import scala.collection.mutable.ReusableBuilder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashMapJmh {
    @Param({"1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private HashMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        ReusableBuilder<Tuple2<Key, Boolean>, HashMap<Key, Boolean>> b = HashMap.newBuilder();
        for (Key key : data.setA) {
            b.addOne(new Tuple2<>(key, Boolean.TRUE));
        }
        mapA = b.result();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (var i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Map<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.$minus(key).$plus(new Tuple2<>(key, Boolean.TRUE));
    }

    @Benchmark
    public Object mPut() {
        Key key = data.nextKeyInA();
        return mapA.$plus(new Tuple2<>(key, Boolean.FALSE));
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return mapA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return mapA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return mapA.head()._1;
    }

    @Benchmark
    public HashMap<Key, Boolean> mTail() {
        return mapA.tail();
    }
}
