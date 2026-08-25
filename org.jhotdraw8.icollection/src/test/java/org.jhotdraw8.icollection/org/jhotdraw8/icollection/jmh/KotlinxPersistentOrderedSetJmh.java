package org.jhotdraw8.icollection.jmh;

import kotlinx.collections.immutable.ExtensionsKt;
import kotlinx.collections.immutable.PersistentSet;
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

/// ```
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// org.jetbrains.kotlin:kotlinx-collections-immutable-jvm:0.5.1
///
/// Benchmark                                              (size)  Mode  Cnt         Score   Error  Units
/// KotlinxPersistentOrderedSetJmh.mAdd                        10  avgt    2       264.255          ns/op
/// KotlinxPersistentOrderedSetJmh.mAdd                      1000  avgt    2     74652.286          ns/op
/// KotlinxPersistentOrderedSetJmh.mAdd                    100000  avgt    2  18466287.515          ns/op
/// KotlinxPersistentOrderedSetJmh.mAddContained               10  avgt    2        14.319          ns/op
/// KotlinxPersistentOrderedSetJmh.mAddContained             1000  avgt    2      6101.824          ns/op
/// KotlinxPersistentOrderedSetJmh.mAddContained           100000  avgt    2   1704048.786          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound              10  avgt    2        11.146          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound            1000  avgt    2      5915.346          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsFound          100000  avgt    2   1784595.752          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound           10  avgt    2         9.448          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound         1000  avgt    2      4665.015          ns/op
/// KotlinxPersistentOrderedSetJmh.mContainsNotFound       100000  avgt    2   1542239.524          ns/op
/// KotlinxPersistentOrderedSetJmh.mCopyOf                     10  avgt    2       222.833          ns/op
/// KotlinxPersistentOrderedSetJmh.mCopyOf                   1000  avgt    2     43845.387          ns/op
/// KotlinxPersistentOrderedSetJmh.mCopyOf                 100000  avgt    2  10233754.311          ns/op
/// KotlinxPersistentOrderedSetJmh.mGetFirst                   10  avgt    2         1.631          ns/op
/// KotlinxPersistentOrderedSetJmh.mGetFirst                 1000  avgt    2         4.484          ns/op
/// KotlinxPersistentOrderedSetJmh.mGetFirst               100000  avgt    2         5.310          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate                    10  avgt    2        42.290          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate                  1000  avgt    2     32214.655          ns/op
/// KotlinxPersistentOrderedSetJmh.mIterate                100000  avgt    2   6509847.976          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemove                     10  avgt    2       266.651          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemove                   1000  avgt    2    124779.056          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemove                 100000  avgt    2  47648473.890          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAll                  10  avgt    2       221.799          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAll                1000  avgt    2     77464.725          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAll              100000  avgt    2  12225593.755          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAllSameType          10  avgt    2       226.661          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAllSameType        1000  avgt    2     74553.890          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveAllSameType      100000  avgt    2  13714369.209          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveFirst                10  avgt    2       229.586          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveFirst              1000  avgt    2     91902.870          ns/op
/// KotlinxPersistentOrderedSetJmh.mRemoveFirst            100000  avgt    2  21511262.500          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllAllRetained       10  avgt    2        69.021          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllAllRetained     1000  avgt    2     27565.963          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllAllRetained   100000  avgt    2   6621041.460          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllNoneRetained      10  avgt    2       217.454          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllNoneRetained    1000  avgt    2     78688.263          ns/op
/// KotlinxPersistentOrderedSetJmh.mRetainAllNoneRetained  100000  avgt    2  12115628.506          ns/op
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentOrderedSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private PersistentSet<Key> setA;
    private PersistentSet<Key> setAA;
    private PersistentSet<Key> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.toPersistentSet(data.setA);
        setAA = ExtensionsKt.toPersistentSet(data.listA);
        setC = ExtensionsKt.toPersistentSet(data.setC);
    }

    @Benchmark
    public PersistentSet<Key> mAdd() {
        PersistentSet<Key> set = ExtensionsKt.persistentSetOf();
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentSet<Key> mAddAll() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.addingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mAddAllSameType() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.addingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mAddContained() {
        PersistentSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public int mContains() {
        int count = 0;
        for (Key k : data.listC) {
            if (setA.contains(k)) count++;
        }
        assert count == data.listC.size() / 2;
        return count;
    }


    @Benchmark
    public boolean mContainsAll() {
        return setA.containsAll(data.setA);
    }

    @Benchmark
    public boolean mContainsAllSameType() {
        return setA.containsAll(setAA);
    }

    @Benchmark
    public PersistentSet<Key> mCopyOf() {
        PersistentSet<Key> set = ExtensionsKt.toPersistentSet(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.iterator().next();
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public PersistentSet<Key> mRemove() {
        PersistentSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveAll() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveAllSameType() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.removingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveFirst() {
        PersistentSet<Key> set = setA;
        while (!set.isEmpty()) {
            set = set.removing(set.iterator().next());
        }
        return set;
    }

    @Benchmark
    public PersistentSet<Key> mRetainAll() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRetainAllSameType() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

}
