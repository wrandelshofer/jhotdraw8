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
import scala.collection.Iterator;
import scala.collection.immutable.HashSet;
import scala.collection.mutable.ReusableBuilder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 1.8.0_345, OpenJDK 64-Bit Server VM, 25.345-b01
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * # org.scala-lang:scala-library:2.13.10
 *
 *                    (size)  Mode  Cnt         Score   Error  Units
 * ContainsFound     1000000  avgt            258.226          ns/op
 * ContainsNotFound  1000000  avgt            213.963          ns/op
 * Head              1000000  avgt             25.830          ns/op
 * Iterate           1000000  avgt       50716705.732          ns/op
 * RemoveAdd         1000000  avgt            809.836          ns/op
 * Tail              1000000  avgt            128.902          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashSetJmh {
    @Param({"1000000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private HashSet<Key> setA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        ReusableBuilder<Key, HashSet<Key>> b = HashSet.newBuilder();
        for (Key key : data.setA) {
            b.addOne(key);
        }
        setA = b.result();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Iterator<Key> i = setA.iterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Object mRemoveAdd() {
        Key key = data.nextKeyInA();
        return setA.$minus(key).$plus(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.head();
    }

    @Benchmark
    public HashSet<Key> mTail() {
        return setA.tail();
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
