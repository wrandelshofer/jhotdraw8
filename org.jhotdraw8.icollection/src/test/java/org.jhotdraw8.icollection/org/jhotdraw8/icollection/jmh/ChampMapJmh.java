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

/**
 * <pre>
 * # JMH version: 1.37
 * # VM version: JDK 21, OpenJDK 64-Bit Server VM, 21+35
 * # Apple M2 Max
 *
 * Benchmark                (mask)  (size)  Mode  Cnt         Score   Error  Units
 * mContainsFound             -65  100000  avgt    2        32.075          ns/op
 * mContainsNotFound          -65  100000  avgt    2        31.649          ns/op
 * mCopyOf                    -65  100000  avgt    2  11764743.641          ns/op
 * mCopyOnyByOne              -65  100000  avgt    2  16341700.025          ns/op
 * mHead                      -65  100000  avgt    2        26.465          ns/op
 * mIterate                   -65  100000  avgt    2   1284574.355          ns/op
 * mPut                       -65  100000  avgt    2       107.241          ns/op
 * mRemoveAll                 -65  100000  avgt    2  14470914.017          ns/op
 * mRemoveOneByOne            -65  100000  avgt    2  18262681.706          ns/op
 * mRemoveThenAdd             -65  100000  avgt    2       231.794          ns/op
 * mRetainAllAllRetained      -65  100000  avgt    2   2880248.290          ns/op
 * mRetainAllNoneRetained     -65  100000  avgt    2   8823159.716          ns/op
 * mTail                      -65  100000  avgt    2        81.924          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampMapJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
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
        for (Key k : mapA.readableKeySet()) {
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
    }
}
