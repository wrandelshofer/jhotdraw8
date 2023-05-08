package org.jhotdraw8.collection.jmh;

import org.jhotdraw8.collection.VectorMap;
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
 * Benchmark                            (mask)  (size)  Mode  Cnt         Score   Error  Units
 * VectorMapJmh.mContainsFound             -65  100000  avgt             69.008          ns/op
 * VectorMapJmh.mContainsNotFound          -65  100000  avgt             62.151          ns/op
 * VectorMapJmh.mCopyOf                    -65  100000  avgt       29209592.309          ns/op
 * VectorMapJmh.mCopyOnyByOne              -65  100000  avgt       33329504.498          ns/op
 * VectorMapJmh.mHead                      -65  100000  avgt             28.013          ns/op
 * VectorMapJmh.mIterate                   -65  100000  avgt        1591652.712          ns/op
 * VectorMapJmh.mPut                       -65  100000  avgt            279.123          ns/op
 * VectorMapJmh.mRemoveAll                 -65  100000  avgt       27430212.184          ns/op
 * VectorMapJmh.mRemoveOneByOne            -65  100000  avgt       78633296.148          ns/op
 * VectorMapJmh.mRemoveThenAdd             -65  100000  avgt            551.835          ns/op
 * VectorMapJmh.mRetainAllAllRetained      -65  100000  avgt        3308725.090          ns/op
 * VectorMapJmh.mRetainAllNoneRetained     -65  100000  avgt       33450610.485          ns/op
 * VectorMapJmh.mTail                      -65  100000  avgt            145.555          ns/op *
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorMapJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private VectorMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = VectorMap.of();
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
    public VectorMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.remove(key).put(key, Boolean.TRUE);
    }

    @Benchmark
    public VectorMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.put(key, Boolean.FALSE);
    }

    @Benchmark
    public VectorMap<Key, Boolean> mCopyOf() {
        return VectorMap.copyOf(data.mapA);
    }

    @Benchmark
    public VectorMap<Key, Boolean> mCopyOnyByOne() {
        VectorMap<Key, Boolean> set = VectorMap.of();
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
    public VectorMap<Key, Boolean> mTail() {
        return mapA.remove(mapA.iterator().next().getKey());
    }

    @Benchmark
    public VectorMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }


    @Benchmark
    public VectorMap<Key, Boolean> mRemoveAll() {
        var updated = mapA.removeAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public VectorMap<Key, Boolean> mRetainAllNoneRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public VectorMap<Key, Boolean> mRetainAllAllRetained() {
        var set = mapA;
        var updated = set.retainAll(data.setA);
        assert updated == mapA;
        return updated;
    }
}
