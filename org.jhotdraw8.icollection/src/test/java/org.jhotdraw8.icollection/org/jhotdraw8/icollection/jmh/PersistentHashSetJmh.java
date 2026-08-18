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
/// /// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// /// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                                    (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentHashSetJmh.mAdd                       -65      10  avgt    2       145.214          ns/op
/// PersistentHashSetJmh.mAdd                       -65    1000  avgt    2     36381.689          ns/op
/// PersistentHashSetJmh.mAdd                       -65  100000  avgt    2  11721594.653          ns/op
/// PersistentHashSetJmh.mContainsFound             -65      10  avgt    2        20.507          ns/op
/// PersistentHashSetJmh.mContainsFound             -65    1000  avgt    2      6454.328          ns/op
/// PersistentHashSetJmh.mContainsFound             -65  100000  avgt    2   1774241.923          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65      10  avgt    2        23.618          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65    1000  avgt    2      4768.093          ns/op
/// PersistentHashSetJmh.mContainsNotFound          -65  100000  avgt    2   1526052.327          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65      10  avgt    2       165.172          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65    1000  avgt    2     34447.710          ns/op
/// PersistentHashSetJmh.mCopyOf                    -65  100000  avgt    2   7821692.950          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65      10  avgt    2         1.022          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65    1000  avgt    2         1.482          ns/op
/// PersistentHashSetJmh.mGetFirst                  -65  100000  avgt    2         6.906          ns/op
/// PersistentHashSetJmh.mIterate                   -65      10  avgt    2        10.088          ns/op
/// PersistentHashSetJmh.mIterate                   -65    1000  avgt    2      1552.284          ns/op
/// PersistentHashSetJmh.mIterate                   -65  100000  avgt    2    516561.987          ns/op
/// PersistentHashSetJmh.mRemove                    -65      10  avgt    2       151.698          ns/op
/// PersistentHashSetJmh.mRemove                    -65    1000  avgt    2     38393.816          ns/op
/// PersistentHashSetJmh.mRemove                    -65  100000  avgt    2  11788660.510          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65      10  avgt    2       160.879          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65    1000  avgt    2     37378.025          ns/op
/// PersistentHashSetJmh.mRemoveAll                 -65  100000  avgt    2   8512400.202          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65      10  avgt    2        48.814          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65    1000  avgt    2      7310.738          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType         -65  100000  avgt    2    681754.168          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65      10  avgt    2        11.820          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65    1000  avgt    2        19.581          ns/op
/// PersistentHashSetJmh.mRemoveFirst               -65  100000  avgt    2        36.999          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65      10  avgt    2        48.291          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     11418.710          ns/op
/// PersistentHashSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   1748979.859          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65      10  avgt    2        87.419          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2     12886.427          ns/op
/// PersistentHashSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2   1837815.641          ns/op
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
