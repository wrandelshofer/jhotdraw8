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

/// <pre>
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
/// KotlinxPersistentHashSetJmh.mAddContained               10  avgt    2        14.786          ns/op
/// KotlinxPersistentHashSetJmh.mAddContained             1000  avgt    2      6664.113          ns/op
/// KotlinxPersistentHashSetJmh.mAddContained           100000  avgt    2   1667419.885          ns/op
/// KotlinxPersistentHashSetJmh.mContainsFound              10  avgt    2        13.568          ns/op
/// KotlinxPersistentHashSetJmh.mContainsFound            1000  avgt    2      5636.819          ns/op
/// KotlinxPersistentHashSetJmh.mContainsFound          100000  avgt    2   1717716.822          ns/op
/// KotlinxPersistentHashSetJmh.mContainsNotFound           10  avgt    2         9.007          ns/op
/// KotlinxPersistentHashSetJmh.mContainsNotFound         1000  avgt    2      4019.352          ns/op
/// KotlinxPersistentHashSetJmh.mContainsNotFound       100000  avgt    2   1354170.886          ns/op
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
/// KotlinxPersistentHashSetJmh.mRemoveAll                  10  avgt    2       313.591          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAll                1000  avgt    2     47662.084          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAll              100000  avgt    2   6276129.235          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType          10  avgt    2        44.066          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType        1000  avgt    2      5743.166          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveAllSameType      100000  avgt    2    727312.461          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst                10  avgt    2        10.621          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst              1000  avgt    2        39.427          ns/op
/// KotlinxPersistentHashSetJmh.mRemoveFirst            100000  avgt    2        80.495          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllAllRetained       10  avgt    2        61.113          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllAllRetained     1000  avgt    2      9641.931          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllAllRetained   100000  avgt    2   1732041.795          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllNoneRetained      10  avgt    2       310.326          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllNoneRetained    1000  avgt    2     49187.754          ns/op
/// KotlinxPersistentHashSetJmh.mRetainAllNoneRetained  100000  avgt    2   6662015.640          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class KotlinxPersistentHashSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private final int mask = ~64;

    private BenchmarkData data;
    private PersistentSet<Key> setA;

    private PersistentSet<Key> setAA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ExtensionsKt.toPersistentHashSet(data.setA);
        setAA = ExtensionsKt.toPersistentHashSet(data.listA);
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
/*
    @Benchmark
    public PersistentSet<Key> mCopyOf() {
        PersistentSet<Key> set = ExtensionsKt.toPersistentHashSet(data.listA);
        assert set.size() == data.listA.size();
        return set;
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
        PersistentSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveAllSameType() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentSet<Key> mRetainAllAllRetained() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRetainAllNoneRetained() {
        PersistentSet<Key> set = setA;
        PersistentSet<Key> updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
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
    public Key mGetFirst() {
        return setA.iterator().next();
    }

    @Benchmark
    public PersistentSet<Key> mRemoveFirst() {
        return setA.removing(setA.iterator().next());
    }

    @Benchmark
    public boolean mContainsFound() {
        boolean found = true;
        for (Key k : data.listA) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }

    @Benchmark
    public boolean mContainsNotFound() {
        boolean found = true;
        for (Key k : data.listB) {
            found = setA.contains(k) & found;//must be long-circuit and operator
        }
        return found;
    }*/
}
