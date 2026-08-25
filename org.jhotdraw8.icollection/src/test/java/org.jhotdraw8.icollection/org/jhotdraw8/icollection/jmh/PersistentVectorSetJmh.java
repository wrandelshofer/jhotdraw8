package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentVectorHashSet;
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
///
/// Benchmark                                      (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentVectorSetJmh.mAdd                       -65      10  avgt    2       198.122          ns/op
/// PersistentVectorSetJmh.mAdd                       -65    1000  avgt    2     45352.420          ns/op
/// PersistentVectorSetJmh.mAdd                       -65  100000  avgt    2  14273256.505          ns/op
/// PersistentVectorSetJmh.mContainsFound             -65      10  avgt    2        40.751          ns/op
/// PersistentVectorSetJmh.mContainsFound             -65    1000  avgt    2      8692.558          ns/op
/// PersistentVectorSetJmh.mContainsFound             -65  100000  avgt    2   2097969.410          ns/op
/// PersistentVectorSetJmh.mContainsNotFound          -65      10  avgt    2        25.168          ns/op
/// PersistentVectorSetJmh.mContainsNotFound          -65    1000  avgt    2      5944.879          ns/op
/// PersistentVectorSetJmh.mContainsNotFound          -65  100000  avgt    2   1572864.921          ns/op
/// PersistentVectorSetJmh.mCopyOf                    -65      10  avgt    2       170.130          ns/op
/// PersistentVectorSetJmh.mCopyOf                    -65    1000  avgt    2     32499.996          ns/op
/// PersistentVectorSetJmh.mCopyOf                    -65  100000  avgt    2   8682782.500          ns/op
/// PersistentVectorSetJmh.mGetFirst                  -65      10  avgt    2         1.113          ns/op
/// PersistentVectorSetJmh.mGetFirst                  -65    1000  avgt    2         1.093          ns/op
/// PersistentVectorSetJmh.mGetFirst                  -65  100000  avgt    2         1.129          ns/op
/// PersistentVectorSetJmh.mIterate                   -65      10  avgt    2        21.679          ns/op
/// PersistentVectorSetJmh.mIterate                   -65    1000  avgt    2      1964.249          ns/op
/// PersistentVectorSetJmh.mIterate                   -65  100000  avgt    2    225573.866          ns/op
/// PersistentVectorSetJmh.mRemove                    -65      10  avgt    2       214.203          ns/op
/// PersistentVectorSetJmh.mRemove                    -65    1000  avgt    2    109692.713          ns/op
/// PersistentVectorSetJmh.mRemove                    -65  100000  avgt    2  33700717.977          ns/op
/// PersistentVectorSetJmh.mRemoveAll                 -65      10  avgt    2       275.619          ns/op
/// PersistentVectorSetJmh.mRemoveAll                 -65    1000  avgt    2     47137.107          ns/op
/// PersistentVectorSetJmh.mRemoveAll                 -65  100000  avgt    2  12164850.157          ns/op
/// PersistentVectorSetJmh.mRemoveAllSameType         -65      10  avgt    2       409.004          ns/op
/// PersistentVectorSetJmh.mRemoveAllSameType         -65    1000  avgt    2    105750.570          ns/op
/// PersistentVectorSetJmh.mRemoveAllSameType         -65  100000  avgt    2  32000360.890          ns/op
/// PersistentVectorSetJmh.mRemoveFirst               -65      10  avgt    2       192.491          ns/op
/// PersistentVectorSetJmh.mRemoveFirst               -65    1000  avgt    2     56988.350          ns/op
/// PersistentVectorSetJmh.mRemoveFirst               -65  100000  avgt    2  14739482.773          ns/op
/// PersistentVectorSetJmh.mRetainAllAllRetained      -65      10  avgt    2        45.691          ns/op
/// PersistentVectorSetJmh.mRetainAllAllRetained      -65    1000  avgt    2      4357.346          ns/op
/// PersistentVectorSetJmh.mRetainAllAllRetained      -65  100000  avgt    2    686318.479          ns/op
/// PersistentVectorSetJmh.mRetainAllNoneRetained     -65      10  avgt    2       374.448          ns/op
/// PersistentVectorSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2     61719.561          ns/op
/// PersistentVectorSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  15833165.804          ns/op
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentVectorSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentVectorHashSet<Key> setA;
    private PersistentVectorHashSet<Key> setAA;
    private PersistentVectorHashSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentVectorHashSet.copyOf(data.setA);
        setB = PersistentVectorHashSet.copyOf(data.listB);
        setAA = PersistentVectorHashSet.copyOf(data.listA);
    }


    @Benchmark
    public PersistentVectorHashSet<Key> mCopyOf() {
        PersistentVectorHashSet<Key> set = PersistentVectorHashSet.copyOf(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public PersistentVectorHashSet<Key> mAdd() {
        PersistentVectorHashSet<Key> set = PersistentVectorHashSet.of();
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentVectorHashSet<Key> mRemove() {
        PersistentVectorHashSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public PersistentVectorHashSet<Key> mRemoveAll() {
        PersistentVectorHashSet<Key> set = setA;
        PersistentVectorHashSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentVectorHashSet<Key> mRemoveAllSameType() {
        PersistentVectorHashSet<Key> set = setA;
        PersistentVectorHashSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentVectorHashSet<Key> mRetainAllAllRetained() {
        PersistentVectorHashSet<Key> set = setA;
        PersistentVectorHashSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentVectorHashSet<Key> mRetainAllNoneRetained() {
        PersistentVectorHashSet<Key> set = setA;
        PersistentVectorHashSet<Key> updated = set.retainingAll(data.setB);
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
    public PersistentVectorHashSet<Key> mRemoveFirst() {
        var s = setA;
        for (int i = 0, n = setA.size(); i < n; i++) {
            s = s.removingFirst();
        }
        return s;
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
    }
}
