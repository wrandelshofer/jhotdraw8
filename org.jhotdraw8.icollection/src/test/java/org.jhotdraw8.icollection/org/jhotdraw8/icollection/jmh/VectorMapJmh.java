package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentVectorHashMap;
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

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                             (size)  Mode  Cnt          Score   Error  Units
/// VectorMapJmh.mContainsFound               10  avgt    2          2.306          ns/op
/// VectorMapJmh.mContainsFound             1000  avgt    2          7.372          ns/op
/// VectorMapJmh.mContainsFound          1000000  avgt    2         77.190          ns/op
/// VectorMapJmh.mContainsNotFound            10  avgt    2          2.306          ns/op
/// VectorMapJmh.mContainsNotFound          1000  avgt    2          7.294          ns/op
/// VectorMapJmh.mContainsNotFound       1000000  avgt    2         71.574          ns/op
/// VectorMapJmh.mCopyOf                      10  avgt    2        293.319          ns/op
/// VectorMapJmh.mCopyOf                    1000  avgt    2      51391.317          ns/op
/// VectorMapJmh.mCopyOf                 1000000  avgt    2  222008399.913          ns/op
/// VectorMapJmh.mCopyOnyByOne                10  avgt    2        270.860          ns/op
/// VectorMapJmh.mCopyOnyByOne              1000  avgt    2      53735.821          ns/op
/// VectorMapJmh.mCopyOnyByOne           1000000  avgt    2  423568359.375          ns/op
/// VectorMapJmh.mHead                        10  avgt    2          1.648          ns/op
/// VectorMapJmh.mHead                      1000  avgt    2          2.089          ns/op
/// VectorMapJmh.mHead                   1000000  avgt    2          3.138          ns/op
/// VectorMapJmh.mIterate                     10  avgt    2          8.059          ns/op
/// VectorMapJmh.mIterate                   1000  avgt    2       2091.059          ns/op
/// VectorMapJmh.mIterate                1000000  avgt    2    9182677.816          ns/op
/// VectorMapJmh.mPut                         10  avgt    2         24.542          ns/op
/// VectorMapJmh.mPut                       1000  avgt    2         59.770          ns/op
/// VectorMapJmh.mPut                    1000000  avgt    2        357.168          ns/op
/// VectorMapJmh.mRemoveAll                   10  avgt    2        251.129          ns/op
/// VectorMapJmh.mRemoveAll                 1000  avgt    2      54772.822          ns/op
/// VectorMapJmh.mRemoveAll              1000000  avgt    2  227823450.129          ns/op
/// VectorMapJmh.mRemoveOneByOne              10  avgt    2        335.259          ns/op
/// VectorMapJmh.mRemoveOneByOne            1000  avgt    2     113540.808          ns/op
/// VectorMapJmh.mRemoveOneByOne         1000000  avgt    2  840973690.958          ns/op
/// VectorMapJmh.mRemoveThenAdd               10  avgt    2         51.918          ns/op
/// VectorMapJmh.mRemoveThenAdd             1000  avgt    2        140.445          ns/op
/// VectorMapJmh.mRemoveThenAdd          1000000  avgt    2        666.683          ns/op
/// VectorMapJmh.mRetainAllAllRetained        10  avgt    2         68.330          ns/op
/// VectorMapJmh.mRetainAllAllRetained      1000  avgt    2       7993.817          ns/op
/// VectorMapJmh.mRetainAllAllRetained   1000000  avgt    2   18593305.362          ns/op
/// VectorMapJmh.mRetainAllNoneRetained       10  avgt    2        362.519          ns/op
/// VectorMapJmh.mRetainAllNoneRetained     1000  avgt    2      70947.929          ns/op
/// VectorMapJmh.mRetainAllNoneRetained  1000000  avgt    2  289822210.714          ns/op
/// VectorMapJmh.mTail                        10  avgt    2         18.779          ns/op
/// VectorMapJmh.mTail                      1000  avgt    2         34.790          ns/op
/// VectorMapJmh.mTail                   1000000  avgt    2         74.329          ns/op
///
/// <pre>
/// # JMH version: 1.36
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
/// Benchmark                            (mask)    (size)  Mode  Cnt       _     Score   Error  Units
/// VectorMapJmh.mContainsFound             -65        10  avgt    2       _     5.549          ns/op
/// VectorMapJmh.mContainsFound             -65      1000  avgt    2       _    17.059          ns/op
/// VectorMapJmh.mContainsFound             -65    100000  avgt    2       _    61.928          ns/op
/// VectorMapJmh.mContainsFound             -65  10000000  avgt    2       _   286.949          ns/op
/// VectorMapJmh.mContainsNotFound          -65        10  avgt    2       _     5.334          ns/op
/// VectorMapJmh.mContainsNotFound          -65      1000  avgt    2       _    16.701          ns/op
/// VectorMapJmh.mContainsNotFound          -65    100000  avgt    2       _    67.041          ns/op
/// VectorMapJmh.mContainsNotFound          -65  10000000  avgt    2       _   274.686          ns/op
/// VectorMapJmh.mCopyOf                    -65        10  avgt    2       _   672.824          ns/op
/// VectorMapJmh.mCopyOf                    -65      1000  avgt    2       _161500.818          ns/op
/// VectorMapJmh.mCopyOf                    -65    100000  avgt    2     28_261003.919          ns/op
/// VectorMapJmh.mCopyOf                    -65  10000000  avgt    2   6517_169466.750          ns/op
/// VectorMapJmh.mCopyOnyByOne              -65        10  avgt    2       _   576.303          ns/op
/// VectorMapJmh.mCopyOnyByOne              -65      1000  avgt    2       _151587.450          ns/op
/// VectorMapJmh.mCopyOnyByOne              -65    100000  avgt    2     34_015399.609          ns/op
/// VectorMapJmh.mCopyOnyByOne              -65  10000000  avgt    2   7707_199395.500          ns/op
/// VectorMapJmh.mHead                      -65        10  avgt    2       _    13.910          ns/op
/// VectorMapJmh.mHead                      -65      1000  avgt    2       _    14.780          ns/op
/// VectorMapJmh.mHead                      -65    100000  avgt    2       _    18.737          ns/op
/// VectorMapJmh.mHead                      -65  10000000  avgt    2       _    19.569          ns/op
/// VectorMapJmh.mIterate                   -65        10  avgt    2       _   128.971          ns/op
/// VectorMapJmh.mIterate                   -65      1000  avgt    2       _ 10397.361          ns/op
/// VectorMapJmh.mIterate                   -65    100000  avgt    2      1_773507.559          ns/op
/// VectorMapJmh.mIterate                   -65  10000000  avgt    2    593_683870.912          ns/op
/// VectorMapJmh.mIterateEnumerator         -65        10  avgt    2       _    52.463          ns/op
/// VectorMapJmh.mIterateEnumerator         -65      1000  avgt    2       _  4473.126          ns/op
/// VectorMapJmh.mIterateEnumerator         -65    100000  avgt    2      1_546238.488          ns/op
/// VectorMapJmh.mIterateEnumerator         -65  10000000  avgt    2    391_293625.519          ns/op
/// VectorMapJmh.mPut                       -65        10  avgt    2       _    39.164          ns/op
/// VectorMapJmh.mPut                       -65      1000  avgt    2       _   119.436          ns/op
/// VectorMapJmh.mPut                       -65    100000  avgt    2       _   278.867          ns/op
/// VectorMapJmh.mPut                       -65  10000000  avgt    2       _   757.197          ns/op
/// VectorMapJmh.mRemoveAll                 -65        10  avgt    2       _   569.006          ns/op
/// VectorMapJmh.mRemoveAll                 -65      1000  avgt    2       _151818.984          ns/op
/// VectorMapJmh.mRemoveAll                 -65    100000  avgt    2     26_904156.302          ns/op
/// VectorMapJmh.mRemoveAll                 -65  10000000  avgt    2   5644_066816.250          ns/op
/// VectorMapJmh.mRemoveOneByOne            -65        10  avgt    2       _   955.478          ns/op
/// VectorMapJmh.mRemoveOneByOne            -65      1000  avgt    2       _326388.382          ns/op
/// VectorMapJmh.mRemoveOneByOne            -65    100000  avgt    2     81_476235.880          ns/op
/// VectorMapJmh.mRemoveOneByOne            -65  10000000  avgt    2  17537_430880.000          ns/op
/// VectorMapJmh.mRemoveThenAdd             -65        10  avgt    2       _   144.023          ns/op
/// VectorMapJmh.mRemoveThenAdd             -65      1000  avgt    2       _   304.386          ns/op
/// VectorMapJmh.mRemoveThenAdd             -65    100000  avgt    2       _   553.679          ns/op
/// VectorMapJmh.mRemoveThenAdd             -65  10000000  avgt    2       _  1189.360          ns/op
/// VectorMapJmh.mRetainAllAllRetained      -65        10  avgt    2       _   269.038          ns/op
/// VectorMapJmh.mRetainAllAllRetained      -65      1000  avgt    2       _ 25698.404          ns/op
/// VectorMapJmh.mRetainAllAllRetained      -65    100000  avgt    2      3_347146.065          ns/op
/// VectorMapJmh.mRetainAllAllRetained      -65  10000000  avgt    2   1079_375404.044          ns/op
/// VectorMapJmh.mRetainAllNoneRetained     -65        10  avgt    2       _   930.607          ns/op
/// VectorMapJmh.mRetainAllNoneRetained     -65      1000  avgt    2       _177402.593          ns/op
/// VectorMapJmh.mRetainAllNoneRetained     -65    100000  avgt    2     33_670087.419          ns/op
/// VectorMapJmh.mRetainAllNoneRetained     -65  10000000  avgt    2   6143_866016.000          ns/op
/// VectorMapJmh.mTail                      -65        10  avgt    2       _    51.236          ns/op
/// VectorMapJmh.mTail                      -65      1000  avgt    2       _    87.100          ns/op
/// VectorMapJmh.mTail                      -65    100000  avgt    2       _   137.688          ns/op
/// VectorMapJmh.mTail                      -65  10000000  avgt    2       _   179.967          ns/op
///
/// Process finished with exit code 0
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorMapJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentVectorHashMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = PersistentVectorHashMap.of();
        for (Key key : data.setA) {
            mapA = mapA.putting(key, Boolean.TRUE);
        }
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : mapA.readableKeySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.removing(key).putting(key, Boolean.TRUE);
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.putting(key, Boolean.FALSE);
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mCopyOf() {
        return PersistentVectorHashMap.copyOf(data.mapA);
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mCopyOnyByOne() {
        PersistentVectorHashMap<Key, Boolean> set = PersistentVectorHashMap.of();
        for (Key key : data.listA) {
            set = set.putting(key, Boolean.FALSE);
        }
        assert set.size() == data.listA.size();
        return set;
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
    public PersistentVectorHashMap<Key, Boolean> mTail() {
        return mapA.removing(mapA.iterator().next().getKey());
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.removing(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }


    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mRemoveAll() {
        var updated = mapA.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mRetainAllNoneRetained() {
        var set = mapA;
        var updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentVectorHashMap<Key, Boolean> mRetainAllAllRetained() {
        var set = mapA;
        var updated = set.retainingAll(data.setA);
        assert updated == mapA;
        return updated;
    }


}
