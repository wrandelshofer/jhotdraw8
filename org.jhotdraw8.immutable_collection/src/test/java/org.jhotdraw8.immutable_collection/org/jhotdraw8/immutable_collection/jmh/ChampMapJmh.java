package org.jhotdraw8.immutable_collection.jmh;

import org.jhotdraw8.immutable_collection.ChampMap;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                           (mask)    (size)  Mode  Cnt           Score   Error  Units
 * ChampMapJmh.mContainsFound             -65        10  avgt                8.135          ns/op
 * ChampMapJmh.mContainsFound             -65      1000  avgt               16.979          ns/op
 * ChampMapJmh.mContainsFound             -65    100000  avgt               57.673          ns/op
 * ChampMapJmh.mContainsFound             -65  10000000  avgt              260.768          ns/op
 * ChampMapJmh.mContainsNotFound          -65        10  avgt                6.709          ns/op
 * ChampMapJmh.mContainsNotFound          -65      1000  avgt               16.938          ns/op
 * ChampMapJmh.mContainsNotFound          -65    100000  avgt               59.807          ns/op
 * ChampMapJmh.mContainsNotFound          -65  10000000  avgt              262.135          ns/op
 * ChampMapJmh.mCopyOf                    -65        10  avgt              477.720          ns/op
 * ChampMapJmh.mCopyOf                    -65      1000  avgt           104483.164          ns/op
 * ChampMapJmh.mCopyOf                    -65    100000  avgt         16088857.957          ns/op
 * ChampMapJmh.mCopyOf                    -65  10000000  avgt       4313203539.000          ns/op
 * ChampMapJmh.mCopyOnyByOne              -65        10  avgt              318.818          ns/op
 * ChampMapJmh.mCopyOnyByOne              -65      1000  avgt            93286.049          ns/op
 * ChampMapJmh.mCopyOnyByOne              -65    100000  avgt         24708346.472          ns/op
 * ChampMapJmh.mCopyOnyByOne              -65  10000000  avgt       5741029507.000          ns/op
 * ChampMapJmh.mHead                      -65        10  avgt                9.839          ns/op
 * ChampMapJmh.mHead                      -65      1000  avgt               12.357          ns/op
 * ChampMapJmh.mHead                      -65    100000  avgt               18.366          ns/op
 * ChampMapJmh.mHead                      -65  10000000  avgt               29.306          ns/op
 * ChampMapJmh.mIterate                   -65        10  avgt              36.719          ns/op
 * ChampMapJmh.mIterate                   -65      1000  avgt             5458.660          ns/op
 * ChampMapJmh.mIterate                   -65    100000  avgt          3442606.147          ns/op
 * ChampMapJmh.mIterate                   -65  10000000  avgt        465335856.500          ns/op
 * ChampMapJmh.mIterateEnumerator         -65        10  avgt               18.694          ns/op
 * ChampMapJmh.mIterateEnumerator         -65      1000  avgt             6114.990          ns/op
 * ChampMapJmh.mIterateEnumerator         -65    100000  avgt          3622574.584          ns/op
 * ChampMapJmh.mIterateEnumerator         -65  10000000  avgt        497013579.095          ns/op
 * ChampMapJmh.mPut                       -65        10  avgt               30.346          ns/op
 * ChampMapJmh.mPut                       -65      1000  avgt               71.152          ns/op
 * ChampMapJmh.mPut                       -65    100000  avgt              151.594          ns/op
 * ChampMapJmh.mPut                       -65  10000000  avgt              657.308          ns/op
 * ChampMapJmh.mRemoveAll                 -65        10  avgt              359.833          ns/op
 * ChampMapJmh.mRemoveAll                 -65      1000  avgt           116705.160          ns/op
 * ChampMapJmh.mRemoveAll                 -65    100000  avgt         23554134.598          ns/op
 * ChampMapJmh.mRemoveAll                 -65  10000000  avgt       4903560584.333          ns/op
 * ChampMapJmh.mRemoveOneByOne            -65        10  avgt              326.347          ns/op
 * ChampMapJmh.mRemoveOneByOne            -65      1000  avgt            97945.848          ns/op
 * ChampMapJmh.mRemoveOneByOne            -65    100000  avgt         26517407.447          ns/op
 * ChampMapJmh.mRemoveOneByOne            -65  10000000  avgt       7446733856.500          ns/op
 * ChampMapJmh.mRemoveThenAdd             -65        10  avgt               68.077          ns/op
 * ChampMapJmh.mRemoveThenAdd             -65      1000  avgt              185.762          ns/op
 * ChampMapJmh.mRemoveThenAdd             -65    100000  avgt              328.524          ns/op
 * ChampMapJmh.mRemoveThenAdd             -65  10000000  avgt              823.737          ns/op
 * ChampMapJmh.mRetainAllAllRetained      -65        10  avgt              131.176          ns/op
 * ChampMapJmh.mRetainAllAllRetained      -65      1000  avgt            18451.559          ns/op
 * ChampMapJmh.mRetainAllAllRetained      -65    100000  avgt          5670748.948          ns/op
 * ChampMapJmh.mRetainAllAllRetained      -65  10000000  avgt       1811336749.167          ns/op
 * ChampMapJmh.mRetainAllNoneRetained     -65        10  avgt              115.168          ns/op
 * ChampMapJmh.mRetainAllNoneRetained     -65      1000  avgt            17648.255          ns/op
 * ChampMapJmh.mRetainAllNoneRetained     -65    100000  avgt          5040221.174          ns/op
 * ChampMapJmh.mRetainAllNoneRetained     -65  10000000  avgt       1759449465.000          ns/op
 * ChampMapJmh.mTail                      -65        10  avgt               29.769          ns/op
 * ChampMapJmh.mTail                      -65      1000  avgt               50.720          ns/op
 * ChampMapJmh.mTail                      -65    100000  avgt               92.446          ns/op
 * ChampMapJmh.mTail                      -65  10000000  avgt              110.721          ns/op
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
    }

 */
}
