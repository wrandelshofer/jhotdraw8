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
/// /// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// /// # Mac Mini M4 Pro, 4.40 GHz
/// /// org.jetbrains.kotlin:kotlinx-collections-immutable-jvm:0.5.1
///
///
/// Benchmark                                           (size)  Mode  Cnt         Score   Error  Units
/// KotlinxPersistentHashSetJmh.mAdd                        10  avgt    2       257.479          ns/op
/// KotlinxPersistentHashSetJmh.mAdd                      1000  avgt    2     72630.152          ns/op
/// KotlinxPersistentHashSetJmh.mAdd                    100000  avgt    2  19153090.652          ns/op
/// KotlinxPersistentHashSetJmh.mAddContained               10  avgt    2        14.764          ns/op
/// KotlinxPersistentHashSetJmh.mAddContained             1000  avgt    2      6805.759          ns/op
/// KotlinxPersistentHashSetJmh.mAddContained           100000  avgt    2   1942697.362          ns/op
/// KotlinxPersistentHashSetJmh.mContains                   10  avgt    2        10.738          ns/op
/// KotlinxPersistentHashSetJmh.mContains                 1000  avgt    2      4720.833          ns/op
/// KotlinxPersistentHashSetJmh.mContains               100000  avgt    2   2043299.412          ns/op
/// KotlinxPersistentHashSetJmh.mContainsAllSameType        10  avgt    2        0.991           ns/op
/// KotlinxPersistentHashSetJmh.mContainsAllSameType      1000  avgt    2        4.137           ns/op
/// KotlinxPersistentHashSetJmh.mContainsAllSameType    100000  avgt    2        5.622           ns/op
/// KotlinxPersistentHashSetJmh.mCopyOf                     10  avgt    2       214.744          ns/op
/// KotlinxPersistentHashSetJmh.mCopyOf                   1000  avgt    2     23275.868          ns/op
/// KotlinxPersistentHashSetJmh.mCopyOf                 100000  avgt    2   6217437.868          ns/op
/// KotlinxPersistentHashSetJmh.mGetFirst                   10  avgt    2         1.130          ns/op
/// KotlinxPersistentHashSetJmh.mGetFirst                 1000  avgt    2        21.155          ns/op
/// KotlinxPersistentHashSetJmh.mGetFirst               100000  avgt    2        34.613          ns/op
/// KotlinxPersistentHashSetJmh.mIterate                    10  avgt    2        40.209          ns/op
/// KotlinxPersistentHashSetJmh.mIterate                  1000  avgt    2      7516.928          ns/op
/// KotlinxPersistentHashSetJmh.mIterate                100000  avgt    2    698104.867          ns/op
/// KotlinxPersistentHashSetJmh.mRemove                     10  avgt    2       128.931          ns/op
/// KotlinxPersistentHashSetJmh.mRemove                   1000  avgt    2     32710.332          ns/op
/// KotlinxPersistentHashSetJmh.mRemove                 100000  avgt    2  10670140.402          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAll                  10  avgt    2       213.688          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAll                1000  avgt    2     30023.479          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAll              100000  avgt    2   5245753.236          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType          10  avgt    2        28.320          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType        1000  avgt    2      9157.864          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType      100000  avgt    2   1119704.383          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAll                  10  avgt    2       228.652          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAll                1000  avgt    2     30482.788          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAll              100000  avgt    2   4825596.266          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllSameType          10  avgt    2        25.430          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllSameType        1000  avgt    2      7070.702          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllSameType      100000  avgt    2    734034.018          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst                10  avgt    2        10.621          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst              1000  avgt    2        39.427          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst            100000  avgt    2        80.495          ns/op
///
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashSetJmh {
    private final int mask = ~64;
    @Param({"10", "1000", "100000"})
    private int size;
    private BenchmarkData data;
    private PersistentSet<Key> setA;
    private PersistentSet<Key> setAA;
    private PersistentSet<Key> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.toPersistentHashSet(data.setA);
        setAA = ExtensionsKt.toPersistentHashSet(data.listA);
        setC = ExtensionsKt.toPersistentHashSet(data.setC);
    }

    @Benchmark
    public PersistentSet<Key> mAdd() {
        PersistentSet<Key> set = ExtensionsKt.persistentHashSetOf();
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
        PersistentSet<Key> set = ExtensionsKt.toPersistentHashSet(data.listA);
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
