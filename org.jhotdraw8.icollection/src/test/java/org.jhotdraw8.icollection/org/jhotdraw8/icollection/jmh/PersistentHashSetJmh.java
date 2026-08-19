package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentHashSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
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
/// Benchmark                                    (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentHashSetJmh.mAdd                       -65      10  avgt    2       181.273          ns/op
/// PersistentHashSetJmh.mAdd                       -65    1000  avgt    2     43283.075          ns/op
/// PersistentHashSetJmh.mAdd                       -65  100000  avgt    2  11504819.911          ns/op
/// PersistentHashSetJmh.mAddContained              -65      10  avgt    2        77.569          ns/op
/// PersistentHashSetJmh.mAddContained              -65    1000  avgt    2     32643.634          ns/op
/// PersistentHashSetJmh.mAddContained              -65  100000  avgt    2   4358871.283          ns/op
/// PersistentHashSetJmh.mContainsFound             -65      10  avgt    2        20.733          ns/op
/// PersistentHashSetJmh.mContainsFound             -65    1000  avgt    2      5692.804          ns/op
/// PersistentHashSetJmh.mContainsFound             -65  100000  avgt    2   1715805.540          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65      10  avgt    2        23.994          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65    1000  avgt    2      4446.425          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65  100000  avgt    2   1456217.097          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65      10  avgt    2       179.614          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65    1000  avgt    2     47004.346          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65  100000  avgt    2   8909278.403          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65      10  avgt    2         1.023          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65    1000  avgt    2         1.457          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65  100000  avgt    2         8.684          ns/op
/// PersistentHashSetJmh.mIterate                   -65      10  avgt    2        10.177          ns/op
/// PersistentHashSetJmh.mIterate                   -65    1000  avgt    2      2958.470          ns/op
/// PersistentHashSetJmh.mIterate                   -65  100000  avgt    2    454123.890          ns/op
/// PersistentHashSetJmh.mRemove                    -65      10  avgt    2       165.223          ns/op
/// PersistentHashSetJmh.mRemove                    -65    1000  avgt    2     49675.240          ns/op
/// PersistentHashSetJmh.mRemove                    -65  100000  avgt    2  13452829.022          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65      10  avgt    2        72.393          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65    1000  avgt    2     15541.162          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65  100000  avgt    2   1983407.792          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65      10  avgt    2        70.312          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65    1000  avgt    2     31624.896          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65  100000  avgt    2   3454655.824          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65      10  avgt    2        13.524          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65    1000  avgt    2        21.159          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65  100000  avgt    2        66.750          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65      10  avgt    2        51.808          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     12897.290          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   1761517.837          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65      10  avgt    2        72.231          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2     14491.321          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2   1947315.931          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentHashSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentHashSet<Key> setA;
    private PersistentHashSet<Key> setAA;
    private PersistentHashSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentHashSet.copyOf(data.setA);
        setB = PersistentHashSet.copyOf(data.listB);
        setAA = PersistentHashSet.copyOf(data.listA);
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
    public PersistentSet<Key> mCopyOf() {
        PersistentSet<Key> set = PersistentHashSet.copyOf(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public PersistentSet<Key> mAdd() {
        PersistentHashSet<Key> set = PersistentHashSet.of();
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
    public PersistentHashSet<Key> mRemoveAll() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRemoveAllSameType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentHashSet<Key> mRetainAllAllRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRetainAllNoneRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(data.setB);
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
    public PersistentHashSet<Key> mRemoveFirst() {
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
    }

}
