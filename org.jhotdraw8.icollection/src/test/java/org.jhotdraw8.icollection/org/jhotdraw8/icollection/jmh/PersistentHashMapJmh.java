package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentHashMap;
import org.jhotdraw8.icollection.PersistentHashMapBuilder;
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

/// <pre>
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                             (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentHashMapJmh.mContainsAll        -65      10  avgt    2        52.920          ns/op
/// PersistentHashMapJmh.mContainsAll        -65    1000  avgt    2      8114.964          ns/op
/// PersistentHashMapJmh.mContainsAll        -65  100000  avgt    2   2546775.750          ns/op
/// PersistentHashMapJmh.mContainsKey        -65      10  avgt    2        21.230          ns/op
/// PersistentHashMapJmh.mContainsKey        -65    1000  avgt    2      5051.891          ns/op
/// PersistentHashMapJmh.mContainsKey        -65  100000  avgt    2   2690374.042          ns/op
/// PersistentHashMapJmh.mCopyOf             -65      10  avgt    2       128.206          ns/op
/// PersistentHashMapJmh.mCopyOf             -65    1000  avgt    2     30643.719          ns/op
/// PersistentHashMapJmh.mCopyOf             -65  100000  avgt    2   7750276.778          ns/op
/// PersistentHashMapJmh.mGetFirst           -65      10  avgt    2         1.265          ns/op
/// PersistentHashMapJmh.mGetFirst           -65    1000  avgt    2         4.754          ns/op
/// PersistentHashMapJmh.mGetFirst           -65  100000  avgt    2        18.634          ns/op
/// PersistentHashMapJmh.mIterate            -65      10  avgt    2         5.286          ns/op
/// PersistentHashMapJmh.mIterate            -65    1000  avgt    2      5592.470          ns/op
/// PersistentHashMapJmh.mIterate            -65  100000  avgt    2    770816.542          ns/op
/// PersistentHashMapJmh.mPut                -65      10  avgt    2       137.422          ns/op
/// PersistentHashMapJmh.mPut                -65    1000  avgt    2     36759.510          ns/op
/// PersistentHashMapJmh.mPut                -65  100000  avgt    2  12243565.236          ns/op
/// PersistentHashMapJmh.mPutAll             -65      10  avgt    2       132.512          ns/op
/// PersistentHashMapJmh.mPutAll             -65    1000  avgt    2     28076.650          ns/op
/// PersistentHashMapJmh.mPutAll             -65  100000  avgt    2   7087879.403          ns/op
/// PersistentHashMapJmh.mPutAllSameType     -65      10  avgt    2        48.788          ns/op
/// PersistentHashMapJmh.mPutAllSameType     -65    1000  avgt    2     10451.884          ns/op
/// PersistentHashMapJmh.mPutAllSameType     -65  100000  avgt    2   1540000.942          ns/op
/// PersistentHashMapJmh.mPutContained       -65      10  avgt    2        33.206          ns/op
/// PersistentHashMapJmh.mPutContained       -65    1000  avgt    2      7722.196          ns/op
/// PersistentHashMapJmh.mPutContained       -65  100000  avgt    2   2187362.155          ns/op
/// PersistentHashMapJmh.mRemove             -65      10  avgt    2       130.033          ns/op
/// PersistentHashMapJmh.mRemove             -65    1000  avgt    2     38888.476          ns/op
/// PersistentHashMapJmh.mRemove             -65  100000  avgt    2  12422081.653          ns/op
/// PersistentHashMapJmh.mRemoveAll          -65      10  avgt    2        92.969          ns/op
/// PersistentHashMapJmh.mRemoveAll          -65    1000  avgt    2     24745.550          ns/op
/// PersistentHashMapJmh.mRemoveAll          -65  100000  avgt    2   4207575.268          ns/op
/// PersistentHashMapJmh.mRemoveFirst        -65      10  avgt    2       121.815          ns/op
/// PersistentHashMapJmh.mRemoveFirst        -65    1000  avgt    2     55442.851          ns/op
/// PersistentHashMapJmh.mRemoveFirst        -65  100000  avgt    2   9847264.000          ns/op
/// PersistentHashMapJmh.mRetainAll          -65      10  avgt    2       113.148          ns/op
/// PersistentHashMapJmh.mRetainAll          -65    1000  avgt    2     26715.364          ns/op
/// PersistentHashMapJmh.mRetainAll          -65  100000  avgt    2   3805151.123          ns/op         ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentHashMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentHashMap<Key, Boolean> setA;
    private PersistentHashMap<Key, Boolean> setAA;
    private PersistentHashMap<Key, Boolean> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentHashMap.of();
        setAA = PersistentHashMap.of();
        setC = PersistentHashMap.of();
        for (Key key : data.setA) {
            setA = setA.putting(key, Boolean.TRUE);
        }
        for (Key key : data.listA) {
            setAA = setAA.putting(key, Boolean.TRUE);
        }
        for (Key key : data.setC) {
            setC = setC.putting(key, Boolean.FALSE);
        }
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mPut() {
        PersistentHashMap<Key, Boolean> set = PersistentHashMap.of();
        for (Key key : data.listA) {
            set = set.putting(key, true);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mPutAll() {
        PersistentHashMap<Key, Boolean> set = setA;
        PersistentHashMap<Key, Boolean> updated = set.puttingAll(data.mapC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mPutAllSameType() {
        PersistentHashMap<Key, Boolean> set = setA;
        PersistentHashMap<Key, Boolean> updated = set.puttingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mPutContained() {
        PersistentHashMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.putting(key, true);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public int mContainsKey() {
        int count = 0;
        for (Key k : data.listC) {
            if (setA.containsKey(k)) count++;
        }
        assert count == data.listC.size() / 2;
        return count;
    }

    @Benchmark
    public boolean mContainsAll() {
        return setA.readableKeySet().containsAll(data.setA);
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mCopyOf() {
        PersistentHashMapBuilder<Key, Boolean> builder = PersistentHashMap.builder();
        builder.putAll(data.mapA);
        PersistentHashMap<Key, Boolean> set = builder.build();
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.readableKeySet().iterator().next();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA.readableKeySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mRemove() {
        PersistentHashMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }


    @Benchmark
    public PersistentHashMap<Key, Boolean> mRemoveAll() {
        PersistentHashMap<Key, Boolean> set = setA;
        PersistentHashMap<Key, Boolean> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mRemoveFirst() {
        PersistentHashMap<Key, Boolean> set = setA;
        while (!set.isEmpty()) {
            set = set.removing(set.readableKeySet().iterator().next());
        }
        return set;
    }

    @Benchmark
    public PersistentHashMap<Key, Boolean> mRetainAll() {
        PersistentHashMap<Key, Boolean> set = setA;
        PersistentHashMap<Key, Boolean> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

}
