package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentVectorMap;
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
/// Benchmark                                          (size)  Mode  Cnt         Score   Error  Units
/// PersistentVectorMapJmh.mContainsFound              10  avgt    2         2.297          ns/op
/// PersistentVectorMapJmh.mContainsFound            1000  avgt    2         7.477          ns/op
/// PersistentVectorMapJmh.mContainsFound          100000  avgt    2        23.159          ns/op
/// PersistentVectorMapJmh.mContainsNotFound           10  avgt    2         2.325          ns/op
/// PersistentVectorMapJmh.mContainsNotFound         1000  avgt    2         7.492          ns/op
/// PersistentVectorMapJmh.mContainsNotFound       100000  avgt    2        23.393          ns/op
/// PersistentVectorMapJmh.mCopyOf                     10  avgt    2       192.378          ns/op
/// PersistentVectorMapJmh.mCopyOf                   1000  avgt    2     33435.655          ns/op
/// PersistentVectorMapJmh.mCopyOf                 100000  avgt    2   8475813.700          ns/op
/// PersistentVectorMapJmh.mCopyOnyByOne               10  avgt    2       227.429          ns/op
/// PersistentVectorMapJmh.mCopyOnyByOne             1000  avgt    2     46286.211          ns/op
/// PersistentVectorMapJmh.mCopyOnyByOne           100000  avgt    2  14899114.805          ns/op
/// PersistentVectorMapJmh.mHead                       10  avgt    2         8.528          ns/op
/// PersistentVectorMapJmh.mHead                     1000  avgt    2         9.591          ns/op
/// PersistentVectorMapJmh.mHead                   100000  avgt    2        10.268          ns/op
/// PersistentVectorMapJmh.mIterate                    10  avgt    2        22.068          ns/op
/// PersistentVectorMapJmh.mIterate                  1000  avgt    2      2031.016          ns/op
/// PersistentVectorMapJmh.mIterate                100000  avgt    2    429926.750          ns/op
/// PersistentVectorMapJmh.mPut                        10  avgt    2        22.869          ns/op
/// PersistentVectorMapJmh.mPut                      1000  avgt    2        68.112          ns/op
/// PersistentVectorMapJmh.mPut                    100000  avgt    2       153.364          ns/op
/// PersistentVectorMapJmh.mRemoveAll                  10  avgt    2       293.300          ns/op
/// PersistentVectorMapJmh.mRemoveAll                1000  avgt    2     57537.235          ns/op
/// PersistentVectorMapJmh.mRemoveAll              100000  avgt    2  16268738.554          ns/op
/// PersistentVectorMapJmh.mRemoveOneByOne             10  avgt    2       300.584          ns/op
/// PersistentVectorMapJmh.mRemoveOneByOne           1000  avgt    2    108184.851          ns/op
/// PersistentVectorMapJmh.mRemoveOneByOne         100000  avgt    2  34021919.573          ns/op
/// PersistentVectorMapJmh.mRemoveThenAdd              10  avgt    2        48.368          ns/op
/// PersistentVectorMapJmh.mRemoveThenAdd            1000  avgt    2       126.083          ns/op
/// PersistentVectorMapJmh.mRemoveThenAdd          100000  avgt    2       241.939          ns/op
/// PersistentVectorMapJmh.mRetainAllAllRetained       10  avgt    2        79.123          ns/op
/// PersistentVectorMapJmh.mRetainAllAllRetained     1000  avgt    2      7241.945          ns/op
/// PersistentVectorMapJmh.mRetainAllAllRetained   100000  avgt    2    916944.539          ns/op
/// PersistentVectorMapJmh.mRetainAllNoneRetained      10  avgt    2       334.427          ns/op
/// PersistentVectorMapJmh.mRetainAllNoneRetained    1000  avgt    2     64600.022          ns/op
/// PersistentVectorMapJmh.mRetainAllNoneRetained  100000  avgt    2  16826422.900          ns/op
/// PersistentVectorMapJmh.mTail                       10  avgt    2        28.303          ns/op
/// PersistentVectorMapJmh.mTail                     1000  avgt    2        43.379          ns/op
/// PersistentVectorMapJmh.mTail                   100000  avgt    2        69.499          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentVectorMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentVectorMap<Key, Boolean> mapA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        mapA = PersistentVectorMap.of();
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
    public PersistentVectorMap<Key, Boolean> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return mapA.removing(key).putting(key, Boolean.TRUE);
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mPut() {
        Key key = data.nextKeyInA();
        return mapA.putting(key, Boolean.FALSE);
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mCopyOf() {
        return PersistentVectorMap.copyOf(data.mapA);
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mCopyOnyByOne() {
        PersistentVectorMap<Key, Boolean> set = PersistentVectorMap.of();
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
    public PersistentVectorMap<Key, Boolean> mTail() {
        return mapA.removing(mapA.iterator().next().getKey());
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mRemoveOneByOne() {
        var map = mapA;
        for (var e : data.listA) {
            map = map.removing(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }


    @Benchmark
    public PersistentVectorMap<Key, Boolean> mRemoveAll() {
        var updated = mapA.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mRetainAllNoneRetained() {
        var set = mapA;
        var updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentVectorMap<Key, Boolean> mRetainAllAllRetained() {
        var set = mapA;
        var updated = set.retainingAll(data.setA);
        assert updated == mapA;
        return updated;
    }


}
