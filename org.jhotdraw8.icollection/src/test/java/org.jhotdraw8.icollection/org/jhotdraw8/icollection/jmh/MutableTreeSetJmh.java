package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.MutableTreeSet;
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
/// Benchmark                                   (mask)  (size)  Mode  Cnt         Score   Error  Units
/// MutableTreeSetJmh.mAdd                       -65      10  avgt    2       129.904          ns/op
/// MutableTreeSetJmh.mAdd                       -65    1000  avgt    2     42828.289          ns/op
/// MutableTreeSetJmh.mAdd                       -65  100000  avgt    2  23610882.764          ns/op
/// MutableTreeSetJmh.mContainsFound             -65      10  avgt    2        43.871          ns/op
/// MutableTreeSetJmh.mContainsFound             -65    1000  avgt    2     15631.840          ns/op
/// MutableTreeSetJmh.mContainsFound             -65  100000  avgt    2  15889937.781          ns/op
/// MutableTreeSetJmh.mContainsNotFound          -65      10  avgt    2        47.763          ns/op
/// MutableTreeSetJmh.mContainsNotFound          -65    1000  avgt    2     15882.949          ns/op
/// MutableTreeSetJmh.mContainsNotFound          -65  100000  avgt    2  17677970.219          ns/op
/// MutableTreeSetJmh.mCopyOf                    -65      10  avgt    2       175.547          ns/op
/// MutableTreeSetJmh.mCopyOf                    -65    1000  avgt    2     43059.993          ns/op
/// MutableTreeSetJmh.mCopyOf                    -65  100000  avgt    2  24247652.599          ns/op
/// MutableTreeSetJmh.mGetFirst                  -65      10  avgt    2         2.188          ns/op
/// MutableTreeSetJmh.mGetFirst                  -65    1000  avgt    2         5.016          ns/op
/// MutableTreeSetJmh.mGetFirst                  -65  100000  avgt    2        11.808          ns/op
/// MutableTreeSetJmh.mIterate                   -65      10  avgt    2        36.590          ns/op
/// MutableTreeSetJmh.mIterate                   -65    1000  avgt    2      2253.290          ns/op
/// MutableTreeSetJmh.mIterate                   -65  100000  avgt    2    822335.270          ns/op
/// MutableTreeSetJmh.mRemove                    -65      10  avgt    2       148.593          ns/op
/// MutableTreeSetJmh.mRemove                    -65    1000  avgt    2     58070.134          ns/op
/// MutableTreeSetJmh.mRemove                    -65  100000  avgt    2  27785662.943          ns/op
/// MutableTreeSetJmh.mRemoveAll                 -65      10  avgt    2       186.546          ns/op
/// MutableTreeSetJmh.mRemoveAll                 -65    1000  avgt    2     51439.983          ns/op
/// MutableTreeSetJmh.mRemoveAll                 -65  100000  avgt    2  11202966.480          ns/op
/// MutableTreeSetJmh.mRemoveAllSameType         -65      10  avgt    2       189.407          ns/op
/// MutableTreeSetJmh.mRemoveAllSameType         -65    1000  avgt    2     54809.198          ns/op
/// MutableTreeSetJmh.mRemoveAllSameType         -65  100000  avgt    2  15715899.072          ns/op
/// MutableTreeSetJmh.mRemoveFirst               -65      10  avgt    2       143.540          ns/op
/// MutableTreeSetJmh.mRemoveFirst               -65    1000  avgt    2     46251.292          ns/op
/// MutableTreeSetJmh.mRemoveFirst               -65  100000  avgt    2   9774139.737          ns/op
/// MutableTreeSetJmh.mRetainAllAllRetained      -65      10  avgt    2        92.442          ns/op
/// MutableTreeSetJmh.mRetainAllAllRetained      -65    1000  avgt    2      5633.744          ns/op
/// MutableTreeSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   1801431.147          ns/op
/// MutableTreeSetJmh.mRetainAllNoneRetained     -65      10  avgt    2       224.057          ns/op
/// MutableTreeSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2     53673.905          ns/op
/// MutableTreeSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  11233542.608          ns/op
/// ```
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableTreeSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private MutableTreeSet<Key> setA;
    private MutableTreeSet<Key> setAA;
    private MutableTreeSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = new MutableTreeSet<>(data.setA);
        setB = new MutableTreeSet<>(data.listB);
        setAA = new MutableTreeSet<>(data.listA);
    }

    @Benchmark
    public MutableTreeSet<Key> mAdd() {
        MutableTreeSet<Key> set = new MutableTreeSet<>();
        for (Key key : data.listA) {
            //noinspection UseBulkOperation
            set.add(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }
/*
    @Benchmark
    public MutableTreeSet<Key> mAddAll() {
        MutableTreeSet<Key> set = new MutableTreeSet<>();
        set.addAll(data.listA);
        assert set.size() == data.listA.size();
        return set;
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

    @Benchmark
    public MutableTreeSet<Key> mCopyOf() {
        MutableTreeSet<Key> set = new MutableTreeSet<>(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.getFirst();
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
    public MutableTreeSet<Key> mRemove() {
        MutableTreeSet<Key> set = setA.clone();
        for (Key key : data.listA) {
            set.remove(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableTreeSet<Key> mRemoveAll() {
        MutableTreeSet<Key> set = setA.clone();
        set.removeAll(data.setA);
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableTreeSet<Key> mRemoveAllSameType() {
        MutableTreeSet<Key> set = setA.clone();
        set.removeAll(setAA);
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableTreeSet<Key> mRemoveFirst() {
        var s = setA.clone();
        for (int i = 0, n = setA.size(); i < n; i++) {
            s.removeFirst();
        }
        return s;
    }

    @Benchmark
    public MutableTreeSet<Key> mRetainAllAllRetained() {
        MutableTreeSet<Key> set = setA.clone();
        set.retainAll(data.setA);
        assert set.size() == setA.size();
        return set;
    }

    @Benchmark
    public MutableTreeSet<Key> mRetainAllNoneRetained() {
        MutableTreeSet<Key> set = setA.clone();
        set.retainAll(data.setB);
        assert set.isEmpty();
        return set;
    }
*/

}
