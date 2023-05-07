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
 * Benchmark                           (mask)  (size)  Mode  Cnt         Score   Error  Units
 * ChampMapJmh.mContainsFound             -65  100000  avgt             63.747          ns/op
 * ChampMapJmh.mContainsNotFound          -65  100000  avgt             52.322          ns/op
 * ChampMapJmh.mCopyOf                    -65  100000  avgt       16596781.617          ns/op
 * ChampMapJmh.mCopyOnyByOne              -65  100000  avgt       23044442.214          ns/op
 * ChampMapJmh.mHead                      -65  100000  avgt             57.160          ns/op
 * ChampMapJmh.mIterate                   -65  100000  avgt        2757300.234          ns/op
 * ChampMapJmh.mPut                       -65  100000  avgt            188.573          ns/op
 * ChampMapJmh.mRemoveAll                 -65  100000  avgt       24564043.819          ns/op
 * ChampMapJmh.mRemoveOneByOne            -65  100000  avgt       27647598.699          ns/op
 * ChampMapJmh.mRemoveThenAdd             -65  100000  avgt            325.072          ns/op
 * ChampMapJmh.mRetainAllAllRetained      -65  100000  avgt        5868854.716          ns/op
 * ChampMapJmh.mRetainAllNoneRetained     -65  100000  avgt        4477554.129          ns/op
 * ChampMapJmh.mTail                      -65  100000  avgt            131.363          ns/op
 *
 * Process finished with exit code 0
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g"})
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
