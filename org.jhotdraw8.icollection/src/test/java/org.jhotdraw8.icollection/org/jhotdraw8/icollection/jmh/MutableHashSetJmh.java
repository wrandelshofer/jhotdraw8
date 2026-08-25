package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.MutableHashSet;
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
/// Benchmark                                 (mask)  (size)  Mode  Cnt        Score   Error  Units
/// MutableHashSetJmh.mAdd                       -65      10  avgt    2      124.567          ns/op
/// MutableHashSetJmh.mAdd                       -65    1000  avgt    2    33681.325          ns/op
/// MutableHashSetJmh.mAdd                       -65  100000  avgt    2  7363675.658          ns/op
/// MutableHashSetJmh.mContainsFound             -65      10  avgt    2       20.542          ns/op
/// MutableHashSetJmh.mContainsFound             -65    1000  avgt    2     6292.956          ns/op
/// MutableHashSetJmh.mContainsFound             -65  100000  avgt    2  1741827.661          ns/op
/// MutableHashSetJmh.mContainsNotFound          -65      10  avgt    2       23.354          ns/op
/// MutableHashSetJmh.mContainsNotFound          -65    1000  avgt    2     4725.020          ns/op
/// MutableHashSetJmh.mContainsNotFound          -65  100000  avgt    2  1486631.890          ns/op
/// MutableHashSetJmh.mCopyOf                    -65      10  avgt    2      149.613          ns/op
/// MutableHashSetJmh.mCopyOf                    -65    1000  avgt    2    34985.204          ns/op
/// MutableHashSetJmh.mCopyOf                    -65  100000  avgt    2  7223963.147          ns/op
/// MutableHashSetJmh.mGetFirst                  -65      10  avgt    2        1.231          ns/op
/// MutableHashSetJmh.mGetFirst                  -65    1000  avgt    2        1.696          ns/op
/// MutableHashSetJmh.mGetFirst                  -65  100000  avgt    2       10.311          ns/op
/// MutableHashSetJmh.mIterate                   -65      10  avgt    2       12.699          ns/op
/// MutableHashSetJmh.mIterate                   -65    1000  avgt    2     3121.329          ns/op
/// MutableHashSetJmh.mIterate                   -65  100000  avgt    2   474259.705          ns/op
/// MutableHashSetJmh.mRemove                    -65      10  avgt    2      119.412          ns/op
/// MutableHashSetJmh.mRemove                    -65    1000  avgt    2    30462.089          ns/op
/// MutableHashSetJmh.mRemove                    -65  100000  avgt    2  7605277.479          ns/op
/// MutableHashSetJmh.mRemoveAll                 -65      10  avgt    2      166.474          ns/op
/// MutableHashSetJmh.mRemoveAll                 -65    1000  avgt    2    37538.597          ns/op
/// MutableHashSetJmh.mRemoveAll                 -65  100000  avgt    2  8869053.859          ns/op
/// MutableHashSetJmh.mRemoveAllSameType         -65      10  avgt    2       28.568          ns/op
/// MutableHashSetJmh.mRemoveAllSameType         -65    1000  avgt    2     7121.718          ns/op
/// MutableHashSetJmh.mRemoveAllSameType         -65  100000  avgt    2   655706.576          ns/op
/// MutableHashSetJmh.mRemoveFirst               -65      10  avgt    2      141.588          ns/op
/// MutableHashSetJmh.mRemoveFirst               -65    1000  avgt    2    50870.903          ns/op
/// MutableHashSetJmh.mRemoveFirst               -65  100000  avgt    2  8519833.269          ns/op
/// MutableHashSetJmh.mRetainAllAllRetained      -65      10  avgt    2       59.075          ns/op
/// MutableHashSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     5353.836          ns/op
/// MutableHashSetJmh.mRetainAllAllRetained      -65  100000  avgt    2  1414502.256          ns/op
/// MutableHashSetJmh.mRetainAllNoneRetained     -65      10  avgt    2      186.721          ns/op
/// MutableHashSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2    42511.754          ns/op
/// MutableHashSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  9361055.457          ns/op
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableHashSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private MutableHashSet<Key> setA;
    private MutableHashSet<Key> setAA;
    private MutableHashSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = new MutableHashSet<>(data.setA);
        setB = new MutableHashSet<>(data.listB);
        setAA = new MutableHashSet<>(data.listA);
    }


    @Benchmark
    public MutableHashSet<Key> mCopyOf() {
        MutableHashSet<Key> set = new MutableHashSet<>(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public MutableHashSet<Key> mAdd() {
        MutableHashSet<Key> set = new MutableHashSet<>();
        for (Key key : data.listA) {
            set.add(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public MutableHashSet<Key> mRemove() {
        MutableHashSet<Key> set = setA.clone();
        for (Key key : data.listA) {
            set.remove(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableHashSet<Key> mRemoveAll() {
        MutableHashSet<Key> set = setA.clone();
        set.removeAll(data.setA);
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableHashSet<Key> mRemoveAllSameType() {
        MutableHashSet<Key> set = setA.clone();
        set.removeAll(setAA);
        assert set.isEmpty();
        return set;
    }


    @Benchmark
    public MutableHashSet<Key> mRetainAllAllRetained() {
        MutableHashSet<Key> set = setA.clone();
        set.retainAll(data.setA);
        assert set.size() == setA.size();
        return set;
    }

    @Benchmark
    public MutableHashSet<Key> mRetainAllNoneRetained() {
        MutableHashSet<Key> set = setA.clone();
        set.retainAll(data.setB);
        assert set.isEmpty();
        return set;
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
    public MutableHashSet<Key> mRemoveFirst() {
        var s = setA.clone();
        for (int i = 0, n = setA.size(); i < n; i++) {
            s.remove(s.iterator().next());
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
