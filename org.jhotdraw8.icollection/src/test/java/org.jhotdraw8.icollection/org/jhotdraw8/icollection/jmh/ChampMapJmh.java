package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.ChampMap;
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
/// Benchmark                           (mask)   (size)  Mode  Cnt          Score   Error  Units
/// ChampMapJmh.mContainsFound             -65       10  avgt    2          2.269          ns/op
/// ChampMapJmh.mContainsFound             -65     1000  avgt    2          7.170          ns/op
/// ChampMapJmh.mContainsFound             -65  1000000  avgt    2         76.946          ns/op
/// ChampMapJmh.mContainsNotFound          -65       10  avgt    2          2.403          ns/op
/// ChampMapJmh.mContainsNotFound          -65     1000  avgt    2          7.672          ns/op
/// ChampMapJmh.mContainsNotFound          -65  1000000  avgt    2         70.197          ns/op
/// ChampMapJmh.mCopyOf                    -65       10  avgt    2        172.033          ns/op
/// ChampMapJmh.mCopyOf                    -65     1000  avgt    2      34440.070          ns/op
/// ChampMapJmh.mCopyOf                    -65  1000000  avgt    2  138012182.932          ns/op
/// ChampMapJmh.mCopyOnyByOne              -65       10  avgt    2        154.888          ns/op
/// ChampMapJmh.mCopyOnyByOne              -65     1000  avgt    2      38538.850          ns/op
/// ChampMapJmh.mCopyOnyByOne              -65  1000000  avgt    2  329488506.726          ns/op
/// ChampMapJmh.mHead                      -65       10  avgt    2          1.114          ns/op
/// ChampMapJmh.mHead                      -65     1000  avgt    2          1.698          ns/op
/// ChampMapJmh.mHead                      -65  1000000  avgt    2         10.334          ns/op
/// ChampMapJmh.mIterateEntry              -65       10  avgt    2         47.315          ns/op
/// ChampMapJmh.mIterateEntry              -65     1000  avgt    2       5948.123          ns/op
/// ChampMapJmh.mIterateEntry              -65  1000000  avgt    2   19583483.919          ns/op
/// ChampMapJmh.mIterateKey                -65       10  avgt    2         22.476          ns/op
/// ChampMapJmh.mIterateKey                -65     1000  avgt    2       3275.111          ns/op
/// ChampMapJmh.mIterateKey                -65  1000000  avgt    2   20461845.731          ns/op
/// ChampMapJmh.mPut                       -65       10  avgt    2         11.598          ns/op
/// ChampMapJmh.mPut                       -65     1000  avgt    2         33.928          ns/op
/// ChampMapJmh.mPut                       -65  1000000  avgt    2        248.478          ns/op
/// ChampMapJmh.mRemoveAll                 -65       10  avgt    2        221.801          ns/op
/// ChampMapJmh.mRemoveAll                 -65     1000  avgt    2      44240.418          ns/op
/// ChampMapJmh.mRemoveAll                 -65  1000000  avgt    2  176013269.009          ns/op
/// ChampMapJmh.mRemoveOneByOne            -65       10  avgt    2        168.691          ns/op
/// ChampMapJmh.mRemoveOneByOne            -65     1000  avgt    2      42232.016          ns/op
/// ChampMapJmh.mRemoveOneByOne            -65  1000000  avgt    2  295290666.647          ns/op
/// ChampMapJmh.mRemoveThenAdd             -65       10  avgt    2         28.202          ns/op
/// ChampMapJmh.mRemoveThenAdd             -65     1000  avgt    2         86.681          ns/op
/// ChampMapJmh.mRemoveThenAdd             -65  1000000  avgt    2        397.161          ns/op
/// ChampMapJmh.mRetainAllAllRetained      -65       10  avgt    2         58.245          ns/op
/// ChampMapJmh.mRetainAllAllRetained      -65     1000  avgt    2       7882.700          ns/op
/// ChampMapJmh.mRetainAllAllRetained      -65  1000000  avgt    2   69312036.924          ns/op
/// ChampMapJmh.mRetainAllNoneRetained     -65       10  avgt    2        271.128          ns/op
/// ChampMapJmh.mRetainAllNoneRetained     -65     1000  avgt    2      46452.643          ns/op
/// ChampMapJmh.mRetainAllNoneRetained     -65  1000000  avgt    2  234638017.930          ns/op
/// ChampMapJmh.mTail                      -65       10  avgt    2         12.659          ns/op
/// ChampMapJmh.mTail                      -65     1000  avgt    2         20.470          ns/op
/// ChampMapJmh.mTail                      -65  1000000  avgt    2         48.780          ns/op
///
/// # JMH version: 1.37
/// # VM version: JDK 21, OpenJDK 64-Bit Server VM, 21+35
/// # Apple M2 Max, 3.70 GHz
///
/// Benchmark                (mask)  (size)  Mode  Cnt         Score   Error  Units
/// mContainsFound             -65  100000  avgt    2        32.075          ns/op
/// mContainsNotFound          -65  100000  avgt    2        31.649          ns/op
/// mCopyOf                    -65  100000  avgt    2  11764743.641          ns/op
/// mCopyOnyByOne              -65  100000  avgt    2  16341700.025          ns/op
/// mHead                      -65  100000  avgt    2        26.465          ns/op
/// mIterate                   -65  100000  avgt    2   1284574.355          ns/op
/// mPut                       -65  100000  avgt    2       107.241          ns/op
/// mRemoveAll                 -65  100000  avgt    2  14470914.017          ns/op
/// mRemoveOneByOne            -65  100000  avgt    2  18262681.706          ns/op
/// mRemoveThenAdd             -65  100000  avgt    2       231.794          ns/op
/// mRetainAllAllRetained      -65  100000  avgt    2   2880248.290          ns/op
/// mRetainAllNoneRetained     -65  100000  avgt    2   8823159.716          ns/op
/// mTail                      -65  100000  avgt    2        81.924          ns/op

@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampMapJmh {
    @Param({"10", "1000", "1000000"})
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
    public int mIterateKey() {
        int sum = 0;
        for (Key k : mapA.readableKeySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public int mIterateEntry() {
        int sum = 0;
        for (var k : mapA.readableEntrySet()) {
            sum += k.getKey().value;
        }
        return sum;
    }

/*
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
    public ChampMap<Key, Boolean> mCopyOnyByOne() {
        ChampMap<Key, Boolean> set = ChampMap.of();
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


    @Benchmark
    public ChampMap<Key, Boolean> mRemoveAll() {
        var updated = mapA.removeAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampMap<Key, Boolean> mRetainAllNoneRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampMap<Key, Boolean> mRetainAllAllRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setA);
        assert updated == mapA;
        return updated;
    }*/
}
