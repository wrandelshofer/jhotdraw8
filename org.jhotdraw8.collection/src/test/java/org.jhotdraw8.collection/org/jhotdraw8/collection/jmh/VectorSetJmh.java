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
 * Benchmark                       (mask)   (size)  Mode  Cnt      _     Score   Error  Units
 * VectorSetJmh.mAdd                  -65       10  avgt    2      _     5.726          ns/op
 * VectorSetJmh.mAdd                  -65      100  avgt    2      _    18.807          ns/op
 * VectorSetJmh.mAdd                  -65     1000  avgt    2      _    28.747          ns/op
 * VectorSetJmh.mAdd                  -65    10000  avgt    2      _    46.049          ns/op
 * VectorSetJmh.mAdd                  -65   100000  avgt    2      _    66.678          ns/op
 * VectorSetJmh.mAdd                  -65  1000000  avgt    2      _   214.538          ns/op
 * VectorSetJmh.mContainsFound        -65       10  avgt    2      _     4.855          ns/op
 * VectorSetJmh.mContainsFound        -65      100  avgt    2      _    11.401          ns/op
 * VectorSetJmh.mContainsFound        -65     1000  avgt    2      _    16.816          ns/op
 * VectorSetJmh.mContainsFound        -65    10000  avgt    2      _    33.466          ns/op
 * VectorSetJmh.mContainsFound        -65   100000  avgt    2      _    52.787          ns/op
 * VectorSetJmh.mContainsFound        -65  1000000  avgt    2      _   196.904          ns/op
 * VectorSetJmh.mContainsNotFound     -65       10  avgt    2      _     4.865          ns/op
 * VectorSetJmh.mContainsNotFound     -65      100  avgt    2      _    11.634          ns/op
 * VectorSetJmh.mContainsNotFound     -65     1000  avgt    2      _    17.090          ns/op
 * VectorSetJmh.mContainsNotFound     -65    10000  avgt    2      _    33.270          ns/op
 * VectorSetJmh.mContainsNotFound     -65   100000  avgt    2      _    52.320          ns/op
 * VectorSetJmh.mContainsNotFound     -65  1000000  avgt    2      _   184.913          ns/op
 * VectorSetJmh.mCopyOf               -65       10  avgt    2      _   633.977          ns/op
 * VectorSetJmh.mCopyOf               -65      100  avgt    2      _  8836.761          ns/op
 * VectorSetJmh.mCopyOf               -65     1000  avgt    2      _146829.800          ns/op
 * VectorSetJmh.mCopyOf               -65    10000  avgt    2     1_817203.463          ns/op
 * VectorSetJmh.mCopyOf               -65   100000  avgt    2    28_280057.915          ns/op
 * VectorSetJmh.mCopyOf               -65  1000000  avgt    2   426_404007.542          ns/op
 * VectorSetJmh.mHead                 -65       10  avgt    2      _    13.308          ns/op
 * VectorSetJmh.mHead                 -65      100  avgt    2      _    14.934          ns/op
 * VectorSetJmh.mHead                 -65     1000  avgt    2      _    14.815          ns/op
 * VectorSetJmh.mHead                 -65    10000  avgt    2      _    17.173          ns/op
 * VectorSetJmh.mHead                 -65   100000  avgt    2      _    18.056          ns/op
 * VectorSetJmh.mHead                 -65  1000000  avgt    2      _    18.003          ns/op
 * VectorSetJmh.mIterate              -65       10  avgt    2      _    62.979          ns/op
 * VectorSetJmh.mIterate              -65      100  avgt    2      _   553.592          ns/op
 * VectorSetJmh.mIterate              -65     1000  avgt    2      _  6405.368          ns/op
 * VectorSetJmh.mIterate              -65    10000  avgt    2      _ 88916.951          ns/op
 * VectorSetJmh.mIterate              -65   100000  avgt    2     1_680524.019          ns/op
 * VectorSetJmh.mIterate              -65  1000000  avgt    2    62_789974.700          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65       10  avgt    2      _  1155.171          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65      100  avgt    2      _ 25438.120          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65     1000  avgt    2      _403014.802          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65    10000  avgt    2     6_901789.117          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65   100000  avgt    2   121_582468.066          ns/op
 * VectorSetJmh.mRemoveOneByOne       -65  1000000  avgt    2  1957_646140.417          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65       10  avgt    2      _   107.353          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65      100  avgt    2      _   167.282          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65     1000  avgt    2      _   254.792          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65    10000  avgt    2      _   351.577          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65   100000  avgt    2      _   477.388          ns/op
 * VectorSetJmh.mRemoveThenAdd        -65  1000000  avgt    2      _   924.429          ns/op
 * VectorSetJmh.mTail                 -65       10  avgt    2      _    38.555          ns/op
 * VectorSetJmh.mTail                 -65      100  avgt    2      _    59.958          ns/op
 * VectorSetJmh.mTail                 -65     1000  avgt    2      _    63.800          ns/op
 * VectorSetJmh.mTail                 -65    10000  avgt    2      _   121.382          ns/op
 * VectorSetJmh.mTail                 -65   100000  avgt    2      _   114.062          ns/op
 * VectorSetJmh.mTail                 -65  1000000  avgt    2      _   137.438          ns/op
 *
 * Process finished with exit code 0
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 0)
@Warmup(iterations = 0)
@Fork(value = 0)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorSetJmh {
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
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

    @Benchmark
    public VectorSet<Key> mRemoveOneByOne() {
        var map = setA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }


}
