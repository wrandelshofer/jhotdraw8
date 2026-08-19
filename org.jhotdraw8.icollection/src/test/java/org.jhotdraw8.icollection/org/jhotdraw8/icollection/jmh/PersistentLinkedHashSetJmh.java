package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentLinkedHashSet;
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
/// Benchmark                                          (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentLinkedHashSetJmh.mAdd                       -65      10  avgt    2       201.441          ns/op
/// PersistentLinkedHashSetJmh.mAdd                       -65    1000  avgt    2     43104.054          ns/op
/// PersistentLinkedHashSetJmh.mAdd                       -65  100000  avgt    2  12825210.300          ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65      10  avgt    2        93.913          ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65    1000  avgt    2     35100.420          ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65  100000  avgt    2   4351937.301          ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65      10  avgt    2        22.887          ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65    1000  avgt    2      5913.895          ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65  100000  avgt    2   1862269.254          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65      10  avgt    2        24.259          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65    1000  avgt    2      4524.820          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65  100000  avgt    2   1585657.994          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65      10  avgt    2       185.742          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65    1000  avgt    2     41535.642          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65  100000  avgt    2   9225537.226          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65      10  avgt    2         1.006          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65    1000  avgt    2         1.608          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65  100000  avgt    2         8.876          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65      10  avgt    2        10.788          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65    1000  avgt    2      2954.753          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65  100000  avgt    2    494787.583          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65      10  avgt    2       164.121          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65    1000  avgt    2     50869.944          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65  100000  avgt    2  14946627.602          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65      10  avgt    2        54.027          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65    1000  avgt    2     14895.420          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65  100000  avgt    2   2104740.013          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65      10  avgt    2        50.309          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65    1000  avgt    2     29848.435          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65  100000  avgt    2   3144518.249          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65      10  avgt    2        90.415          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     17892.542          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   2639516.189          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65      10  avgt    2        49.823          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2     13385.875          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2   2245993.397          ns/op         ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentLinkedHashSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentLinkedHashSet<Key> setA;
    private PersistentLinkedHashSet<Key> setAA;
    private PersistentLinkedHashSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentLinkedHashSet.copyOf(data.setA);
        setB = PersistentLinkedHashSet.copyOf(data.listB);
        setAA = PersistentLinkedHashSet.copyOf(data.listA);
    }

    /*
        @Benchmark
        public PersistentLinkedHashSet<Key> mCopyOf() {
            PersistentLinkedHashSet<Key> set = PersistentLinkedHashSet.copyOf(data.listA);
            assert set.size() == data.listA.size();
            return set;
        }


        @Benchmark
        public PersistentLinkedHashSet<Key> mAdd() {
            PersistentLinkedHashSet<Key> set = PersistentLinkedHashSet.of();
            for (Key key : data.listA) {
                set = set.adding(key);
            }
            assert set.size() == data.listA.size();
            return set;
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
    public PersistentLinkedHashSet<Key> mRemove() {
        PersistentLinkedHashSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRemoveAll() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRemoveAllSameType() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentLinkedHashSet<Key> mRetainAllAllRetained() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRetainAllNoneRetained() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.retainingAll(data.setB);
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
        return setA.getFirst();
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRemoveFirst() {
        var s = setA;
        for (int i = 0, n = setA.size(); i < n; i++) {
            s = s.removingFirst();
        }
        return s;
    }
*/
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
    }

}
