package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentMap;
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
/// org.jetbrains.kotlin:kotlinx-collections-immutable-jvm:0.5.1
///
/// Benchmark                                         (mask)  (size)  Mode  Cnt         Score   Error  Units
/// KotlinxPersistentHashMapJmh.mContainsAll             -65      10  avgt    2        35.990          ns/op
/// KotlinxPersistentHashMapJmh.mContainsAll             -65    1000  avgt    2      7271.252          ns/op
/// KotlinxPersistentHashMapJmh.mContainsAll             -65  100000  avgt    2   2385519.325          ns/op
/// KotlinxPersistentHashMapJmh.mContainsAllSameType     -65      10  avgt    2        37.764          ns/op
/// KotlinxPersistentHashMapJmh.mContainsAllSameType     -65    1000  avgt    2      8586.642          ns/op
/// KotlinxPersistentHashMapJmh.mContainsAllSameType     -65  100000  avgt    2   1662283.503          ns/op
/// KotlinxPersistentHashMapJmh.mContainsKey             -65      10  avgt    2        10.228          ns/op
/// KotlinxPersistentHashMapJmh.mContainsKey             -65    1000  avgt    2      5147.611          ns/op
/// KotlinxPersistentHashMapJmh.mContainsKey             -65  100000  avgt    2   2427384.373          ns/op
/// KotlinxPersistentHashMapJmh.mCopyOf                  -65      10  avgt    2       153.298          ns/op
/// KotlinxPersistentHashMapJmh.mCopyOf                  -65    1000  avgt    2     31852.741          ns/op
/// KotlinxPersistentHashMapJmh.mCopyOf                  -65  100000  avgt    2   8222299.204          ns/op
/// KotlinxPersistentHashMapJmh.mGetFirst                -65      10  avgt    2        10.146          ns/op
/// KotlinxPersistentHashMapJmh.mGetFirst                -65    1000  avgt    2        14.781          ns/op
/// KotlinxPersistentHashMapJmh.mGetFirst                -65  100000  avgt    2        16.979          ns/op
/// KotlinxPersistentHashMapJmh.mIterate                 -65      10  avgt    2        39.632          ns/op
/// KotlinxPersistentHashMapJmh.mIterate                 -65    1000  avgt    2      3771.418          ns/op
/// KotlinxPersistentHashMapJmh.mIterate                 -65  100000  avgt    2    409702.381          ns/op
/// KotlinxPersistentHashMapJmh.mPut                     -65      10  avgt    2       147.816          ns/op
/// KotlinxPersistentHashMapJmh.mPut                     -65    1000  avgt    2     35535.000          ns/op
/// KotlinxPersistentHashMapJmh.mPut                     -65  100000  avgt    2  12500930.015          ns/op
/// KotlinxPersistentHashMapJmh.mPutAll                  -65      10  avgt    2       136.031          ns/op
/// KotlinxPersistentHashMapJmh.mPutAll                  -65    1000  avgt    2     26329.174          ns/op
/// KotlinxPersistentHashMapJmh.mPutAll                  -65  100000  avgt    2   7316476.838          ns/op
/// KotlinxPersistentHashMapJmh.mPutAllSameType          -65      10  avgt    2        48.520          ns/op
/// KotlinxPersistentHashMapJmh.mPutAllSameType          -65    1000  avgt    2      9404.159          ns/op
/// KotlinxPersistentHashMapJmh.mPutAllSameType          -65  100000  avgt    2   1518075.057          ns/op
/// KotlinxPersistentHashMapJmh.mPutContained            -65      10  avgt    2        13.647          ns/op
/// KotlinxPersistentHashMapJmh.mPutContained            -65    1000  avgt    2      7578.176          ns/op
/// KotlinxPersistentHashMapJmh.mPutContained            -65  100000  avgt    2   2023279.384          ns/op
/// KotlinxPersistentHashMapJmh.mRemove                  -65      10  avgt    2       132.027          ns/op
/// KotlinxPersistentHashMapJmh.mRemove                  -65    1000  avgt    2     39719.490          ns/op
/// KotlinxPersistentHashMapJmh.mRemove                  -65  100000  avgt    2  12847214.279          ns/op
/// KotlinxPersistentHashMapJmh.mRemoveFirst             -65      10  avgt    2       245.384          ns/op
/// KotlinxPersistentHashMapJmh.mRemoveFirst             -65    1000  avgt    2     53918.664          ns/op
/// KotlinxPersistentHashMapJmh.mRemoveFirst             -65  100000  avgt    2   9764591.609          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashMapJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentMap<Key, Boolean> setA;
    private PersistentMap<Key, Boolean> setAA;
    private PersistentMap<Key, Boolean> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.persistentHashMapOf();
        setAA = ExtensionsKt.persistentHashMapOf();
        setC = ExtensionsKt.persistentHashMapOf();
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
    public PersistentMap<Key, Boolean> mPut() {
        PersistentMap<Key, Boolean> set = ExtensionsKt.persistentHashMapOf();
        for (Key key : data.listA) {
            set = set.putting(key, true);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutAll() {
        PersistentMap<Key, Boolean> set = setA;
        PersistentMap<Key, Boolean> updated = set.puttingAll(data.mapC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutAllSameType() {
        PersistentMap<Key, Boolean> set = setA;
        PersistentMap<Key, Boolean> updated = set.puttingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mPutContained() {
        PersistentMap<Key, Boolean> set = setA;
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
        return setA.keySet().containsAll(data.setA);
    }

    @Benchmark
    public boolean mContainsAllSameType() {
        return setA.keySet().containsAll(setAA.keySet());
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mCopyOf() {
        PersistentMap.Builder<Key, Boolean> builder = ExtensionsKt.<Key, Boolean>persistentHashMapOf().builder();
        builder.putAll(data.mapA);
        PersistentMap<Key, Boolean> set = builder.build();
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.keySet().iterator().next();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA.keySet()) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentMap<Key, Boolean> mRemove() {
        PersistentMap<Key, Boolean> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    /*

    @Benchmark
    public PersistentMap<Key,Boolean> mRemoveAll() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key,Boolean> mRemoveAllSameType() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.removingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }*/

    @Benchmark
    public PersistentMap<Key, Boolean> mRemoveFirst() {
        PersistentMap<Key, Boolean> set = setA;
        while (!set.isEmpty()) {
            set = set.removing(set.keySet().iterator().next());
        }
        return set;
    }
/*
    @Benchmark
    public PersistentMap<Key,Boolean> mRetainAll() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentMap<Key,Boolean> mRetainAllSameType() {
        PersistentMap<Key,Boolean> set = setA;
        PersistentMap<Key,Boolean> updated = set.retainingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }*/
}
