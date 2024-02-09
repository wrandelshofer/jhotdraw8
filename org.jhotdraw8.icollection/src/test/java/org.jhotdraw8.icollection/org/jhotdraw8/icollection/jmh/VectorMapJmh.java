package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.ChampVectorMap;
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
 * Benchmark                            (mask)    (size)  Mode  Cnt       _     Score   Error  Units
 * VectorMapJmh.mContainsFound             -65        10  avgt    2       _     5.549          ns/op
 * VectorMapJmh.mContainsFound             -65      1000  avgt    2       _    17.059          ns/op
 * VectorMapJmh.mContainsFound             -65    100000  avgt    2       _    61.928          ns/op
 * VectorMapJmh.mContainsFound             -65  10000000  avgt    2       _   286.949          ns/op
 * VectorMapJmh.mContainsNotFound          -65        10  avgt    2       _     5.334          ns/op
 * VectorMapJmh.mContainsNotFound          -65      1000  avgt    2       _    16.701          ns/op
 * VectorMapJmh.mContainsNotFound          -65    100000  avgt    2       _    67.041          ns/op
 * VectorMapJmh.mContainsNotFound          -65  10000000  avgt    2       _   274.686          ns/op
 * VectorMapJmh.mCopyOf                    -65        10  avgt    2       _   672.824          ns/op
 * VectorMapJmh.mCopyOf                    -65      1000  avgt    2       _161500.818          ns/op
 * VectorMapJmh.mCopyOf                    -65    100000  avgt    2     28_261003.919          ns/op
 * VectorMapJmh.mCopyOf                    -65  10000000  avgt    2   6517_169466.750          ns/op
 * VectorMapJmh.mCopyOnyByOne              -65        10  avgt    2       _   576.303          ns/op
 * VectorMapJmh.mCopyOnyByOne              -65      1000  avgt    2       _151587.450          ns/op
 * VectorMapJmh.mCopyOnyByOne              -65    100000  avgt    2     34_015399.609          ns/op
 * VectorMapJmh.mCopyOnyByOne              -65  10000000  avgt    2   7707_199395.500          ns/op
 * VectorMapJmh.mHead                      -65        10  avgt    2       _    13.910          ns/op
 * VectorMapJmh.mHead                      -65      1000  avgt    2       _    14.780          ns/op
 * VectorMapJmh.mHead                      -65    100000  avgt    2       _    18.737          ns/op
 * VectorMapJmh.mHead                      -65  10000000  avgt    2       _    19.569          ns/op
 * VectorMapJmh.mIterate                   -65        10  avgt    2       _   128.971          ns/op
 * VectorMapJmh.mIterate                   -65      1000  avgt    2       _ 10397.361          ns/op
 * VectorMapJmh.mIterate                   -65    100000  avgt    2      1_773507.559          ns/op
 * VectorMapJmh.mIterate                   -65  10000000  avgt    2    593_683870.912          ns/op
 * VectorMapJmh.mIterateEnumerator         -65        10  avgt    2       _    52.463          ns/op
 * VectorMapJmh.mIterateEnumerator         -65      1000  avgt    2       _  4473.126          ns/op
 * VectorMapJmh.mIterateEnumerator         -65    100000  avgt    2      1_546238.488          ns/op
 * VectorMapJmh.mIterateEnumerator         -65  10000000  avgt    2    391_293625.519          ns/op
 * VectorMapJmh.mPut                       -65        10  avgt    2       _    39.164          ns/op
 * VectorMapJmh.mPut                       -65      1000  avgt    2       _   119.436          ns/op
 * VectorMapJmh.mPut                       -65    100000  avgt    2       _   278.867          ns/op
 * VectorMapJmh.mPut                       -65  10000000  avgt    2       _   757.197          ns/op
 * VectorMapJmh.mRemoveAll                 -65        10  avgt    2       _   569.006          ns/op
 * VectorMapJmh.mRemoveAll                 -65      1000  avgt    2       _151818.984          ns/op
 * VectorMapJmh.mRemoveAll                 -65    100000  avgt    2     26_904156.302          ns/op
 * VectorMapJmh.mRemoveAll                 -65  10000000  avgt    2   5644_066816.250          ns/op
 * VectorMapJmh.mRemoveOneByOne            -65        10  avgt    2       _   955.478          ns/op
 * VectorMapJmh.mRemoveOneByOne            -65      1000  avgt    2       _326388.382          ns/op
 * VectorMapJmh.mRemoveOneByOne            -65    100000  avgt    2     81_476235.880          ns/op
 * VectorMapJmh.mRemoveOneByOne            -65  10000000  avgt    2  17537_430880.000          ns/op
 * VectorMapJmh.mRemoveThenAdd             -65        10  avgt    2       _   144.023          ns/op
 * VectorMapJmh.mRemoveThenAdd             -65      1000  avgt    2       _   304.386          ns/op
 * VectorMapJmh.mRemoveThenAdd             -65    100000  avgt    2       _   553.679          ns/op
 * VectorMapJmh.mRemoveThenAdd             -65  10000000  avgt    2       _  1189.360          ns/op
 * VectorMapJmh.mRetainAllAllRetained      -65        10  avgt    2       _   269.038          ns/op
 * VectorMapJmh.mRetainAllAllRetained      -65      1000  avgt    2       _ 25698.404          ns/op
 * VectorMapJmh.mRetainAllAllRetained      -65    100000  avgt    2      3_347146.065          ns/op
 * VectorMapJmh.mRetainAllAllRetained      -65  10000000  avgt    2   1079_375404.044          ns/op
 * VectorMapJmh.mRetainAllNoneRetained     -65        10  avgt    2       _   930.607          ns/op
 * VectorMapJmh.mRetainAllNoneRetained     -65      1000  avgt    2       _177402.593          ns/op
 * VectorMapJmh.mRetainAllNoneRetained     -65    100000  avgt    2     33_670087.419          ns/op
 * VectorMapJmh.mRetainAllNoneRetained     -65  10000000  avgt    2   6143_866016.000          ns/op
 * VectorMapJmh.mTail                      -65        10  avgt    2       _    51.236          ns/op
 * VectorMapJmh.mTail                      -65      1000  avgt    2       _    87.100          ns/op
 * VectorMapJmh.mTail                      -65    100000  avgt    2       _   137.688          ns/op
 * VectorMapJmh.mTail                      -65  10000000  avgt    2       _   179.967          ns/op
 *
 * Process finished with exit code 0
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorMapJmh {
    @Param({"10", "1000", "100000", "10000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private ChampVectorMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = ChampVectorMap.of();
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
    public ChampVectorMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.put(key, Boolean.FALSE);
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mCopyOf() {
        return ChampVectorMap.copyOf(data.mapA);
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mCopyOnyByOne() {
        ChampVectorMap<Key, Boolean> set = ChampVectorMap.of();
        for (Key key : data.listA) {
            set = set.put(key, Boolean.FALSE);
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
    public ChampVectorMap<Key, Boolean> mTail() {
        return mapA.remove(mapA.iterator().next().getKey());
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }


    @Benchmark
    public ChampVectorMap<Key, Boolean> mRemoveAll() {
        var updated = mapA.removeAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mRetainAllNoneRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampVectorMap<Key, Boolean> mRetainAllAllRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setA);
        assert updated == mapA;
        return updated;
    }


}
