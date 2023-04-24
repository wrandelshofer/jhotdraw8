package org.jhotdraw8.collection.jmh;

import org.jhotdraw8.collection.ChampMap;
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
 * Benchmark                    (mask)   (size)  Mode  Cnt           Score   Error  Units
 * ChampMapJmh.mContainsFound                10  avgt    2     _     5.597          ns/op
 * ChampMapJmh.mContainsFound               100  avgt    2     _    11.256          ns/op
 * ChampMapJmh.mContainsFound              1000  avgt    2     _    18.196          ns/op
 * ChampMapJmh.mContainsFound             10000  avgt    2     _    33.694          ns/op
 * ChampMapJmh.mContainsFound            100000  avgt    2     _    62.333          ns/op
 * ChampMapJmh.mContainsFound           1000000  avgt    2     _   195.234          ns/op
 * ChampMapJmh.mContainsNotFound             10  avgt    2     _     5.415          ns/op
 * ChampMapJmh.mContainsNotFound            100  avgt    2     _    10.863          ns/op
 * ChampMapJmh.mContainsNotFound           1000  avgt    2     _    17.988          ns/op
 * ChampMapJmh.mContainsNotFound          10000  avgt    2     _    33.044          ns/op
 * ChampMapJmh.mContainsNotFound         100000  avgt    2     _    59.870          ns/op
 * ChampMapJmh.mContainsNotFound        1000000  avgt    2     _   189.715          ns/op
 * ChampMapJmh.mCopyOf                       10  avgt    2     _   390.873          ns/op
 * ChampMapJmh.mCopyOf                      100  avgt    2     _  5541.839          ns/op
 * ChampMapJmh.mCopyOf                     1000  avgt    2     _ 98498.383          ns/op
 * ChampMapJmh.mCopyOf                    10000  avgt    2    1_134498.063          ns/op
 * ChampMapJmh.mCopyOf                   100000  avgt    2   16_766068.600          ns/op
 * ChampMapJmh.mCopyOf                  1000000  avgt    2  309_862370.167          ns/op
 * ChampMapJmh.mHead                         10  avgt    2     _    19.239          ns/op
 * ChampMapJmh.mHead                        100  avgt    2     _    32.538          ns/op
 * ChampMapJmh.mHead                       1000  avgt    2     _    33.179          ns/op
 * ChampMapJmh.mHead                      10000  avgt    2     _    40.673          ns/op
 * ChampMapJmh.mHead                     100000  avgt    2     _    49.164          ns/op
 * ChampMapJmh.mHead                    1000000  avgt    2     _    58.482          ns/op
 * ChampMapJmh.mIterate                      10  avgt    2     _   129.451          ns/op
 * ChampMapJmh.mIterate                     100  avgt    2     _  1760.090          ns/op
 * ChampMapJmh.mIterate                    1000  avgt    2     _ 18662.487          ns/op
 * ChampMapJmh.mIterate                   10000  avgt    2     _195351.055          ns/op
 * ChampMapJmh.mIterate                  100000  avgt    2    2_960985.104          ns/op
 * ChampMapJmh.mIterate                 1000000  avgt    2   62_507410.945          ns/op
 * ChampMapJmh.mPut                          10  avgt    2     _    28.167          ns/op
 * ChampMapJmh.mPut                         100  avgt    2     _    45.893          ns/op
 * ChampMapJmh.mPut                        1000  avgt    2     _    69.583          ns/op
 * ChampMapJmh.mPut                       10000  avgt    2     _   101.107          ns/op
 * ChampMapJmh.mPut                      100000  avgt    2     _   167.333          ns/op
 * ChampMapJmh.mPut                     1000000  avgt    2     _   365.301          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65       10  avgt    2     _   445.596          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65      100  avgt    2     _  6807.719          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65     1000  avgt    2     _111277.355          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65    10000  avgt    2    1_704897.988          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65   100000  avgt    2   32_625441.415          ns/op
 * ChampMapJmh.mRemoveOneByOne     -65  1000000  avgt    2  441_169472.587          ns/op
 * ChampMapJmh.mRemoveThenAdd                10  avgt    2     _    75.010          ns/op
 * ChampMapJmh.mRemoveThenAdd               100  avgt    2         118.792          ns/op
 * ChampMapJmh.mRemoveThenAdd              1000  avgt    2         185.133          ns/op
 * ChampMapJmh.mRemoveThenAdd             10000  avgt    2         270.122          ns/op
 * ChampMapJmh.mRemoveThenAdd            100000  avgt    2         331.028          ns/op
 * ChampMapJmh.mRemoveThenAdd           1000000  avgt    2         576.545          ns/op
 * ChampMapJmh.mTail                         10  avgt    2          52.106          ns/op
 * ChampMapJmh.mTail                        100  avgt    2          84.184          ns/op
 * ChampMapJmh.mTail                       1000  avgt    2          84.854          ns/op
 * ChampMapJmh.mTail                      10000  avgt    2         107.165          ns/op
 * ChampMapJmh.mTail                     100000  avgt    2         131.376          ns/op
 * ChampMapJmh.mTail                    1000000  avgt    2         159.669          ns/op
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
public class ChampMapJmh {
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private ChampMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = ChampMap.of();
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
    public ChampMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public ChampMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.put(key, Boolean.FALSE);
    }

    @Benchmark
    public ChampMap<Key, Boolean> mCopyOf() {
        return ChampMap.copyOf(data.mapA);
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
    public ChampMap<Key, Boolean> mTail() {
        return mapA.remove(mapA.iterator().next().getKey());
    }

    @Benchmark
    public ChampMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }
}
