package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.MutableLinkedHashSetWithLinkedElement;
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
/// Benchmark                                   (mask)  (size)  Mode  Cnt         Score   Error  Units
/// MutableLinkedSetJmh.mAdd                       -65      10  avgt    2       160.719          ns/op
/// MutableLinkedSetJmh.mAdd                       -65    1000  avgt    2     32876.342          ns/op
/// MutableLinkedSetJmh.mAdd                       -65  100000  avgt    2   8400590.443          ns/op
/// MutableLinkedSetJmh.mAddAll                    -65      10  avgt    2       187.297          ns/op
/// MutableLinkedSetJmh.mAddAll                    -65    1000  avgt    2     34351.574          ns/op
/// MutableLinkedSetJmh.mAddAll                    -65  100000  avgt    2   8996120.258          ns/op
/// MutableLinkedSetJmh.mContainsFound             -65      10  avgt    2        39.435          ns/op
/// MutableLinkedSetJmh.mContainsFound             -65    1000  avgt    2      7038.873          ns/op
/// MutableLinkedSetJmh.mContainsFound             -65  100000  avgt    2   2080247.784          ns/op
/// MutableLinkedSetJmh.mContainsNotFound          -65      10  avgt    2        25.121          ns/op
/// MutableLinkedSetJmh.mContainsNotFound          -65    1000  avgt    2      6331.998          ns/op
/// MutableLinkedSetJmh.mContainsNotFound          -65  100000  avgt    2   1761203.377          ns/op
/// MutableLinkedSetJmh.mCopyOf                    -65      10  avgt    2       191.730          ns/op
/// MutableLinkedSetJmh.mCopyOf                    -65    1000  avgt    2     35615.994          ns/op
/// MutableLinkedSetJmh.mCopyOf                    -65  100000  avgt    2   9099912.571          ns/op
/// MutableLinkedSetJmh.mGetFirst                  -65      10  avgt    2         0.628          ns/op
/// MutableLinkedSetJmh.mGetFirst                  -65    1000  avgt    2         0.627          ns/op
/// MutableLinkedSetJmh.mGetFirst                  -65  100000  avgt    2         0.619          ns/op
/// MutableLinkedSetJmh.mIterate                   -65      10  avgt    2        96.125          ns/op
/// MutableLinkedSetJmh.mIterate                   -65    1000  avgt    2     29289.729          ns/op
/// MutableLinkedSetJmh.mIterate                   -65  100000  avgt    2   6100867.273          ns/op
/// MutableLinkedSetJmh.mRemove                    -65      10  avgt    2       410.133          ns/op
/// MutableLinkedSetJmh.mRemove                    -65    1000  avgt    2     85633.939          ns/op
/// MutableLinkedSetJmh.mRemove                    -65  100000  avgt    2  22586406.696          ns/op
/// MutableLinkedSetJmh.mRemoveAll                 -65      10  avgt    2       424.158          ns/op
/// MutableLinkedSetJmh.mRemoveAll                 -65    1000  avgt    2    100294.384          ns/op
/// MutableLinkedSetJmh.mRemoveAll                 -65  100000  avgt    2  20266437.907          ns/op
/// MutableLinkedSetJmh.mRemoveAllSameType         -65      10  avgt    2       444.633          ns/op
/// MutableLinkedSetJmh.mRemoveAllSameType         -65    1000  avgt    2    107550.626          ns/op
/// MutableLinkedSetJmh.mRemoveAllSameType         -65  100000  avgt    2  31478833.220          ns/op
/// MutableLinkedSetJmh.mRemoveFirst               -65      10  avgt    2       678.885          ns/op
/// MutableLinkedSetJmh.mRemoveFirst               -65    1000  avgt    2    120237.446          ns/op
/// MutableLinkedSetJmh.mRemoveFirst               -65  100000  avgt    2  21071545.165          ns/op
/// MutableLinkedSetJmh.mRetainAllAllRetained      -65      10  avgt    2       113.853          ns/op
/// MutableLinkedSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     29359.411          ns/op
/// MutableLinkedSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   5782603.702          ns/op
/// MutableLinkedSetJmh.mRetainAllNoneRetained     -65      10  avgt    2       592.669          ns/op
/// MutableLinkedSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2    118509.174          ns/op
/// MutableLinkedSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  21675718.508          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableLinkedSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private MutableLinkedHashSetWithLinkedElement<Key> setA;
    private MutableLinkedHashSetWithLinkedElement<Key> setAA;
    private MutableLinkedHashSetWithLinkedElement<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = new MutableLinkedHashSetWithLinkedElement<>(data.setA);
        setB = new MutableLinkedHashSetWithLinkedElement<>(data.listB);
        setAA = new MutableLinkedHashSetWithLinkedElement<>(data.listA);
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mAdd() {
        MutableLinkedHashSetWithLinkedElement<Key> set = new MutableLinkedHashSetWithLinkedElement<>();
        for (Key key : data.listA) {
            //noinspection UseBulkOperation
            set.add(key);
        }
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
    public MutableLinkedHashSetWithLinkedElement<Key> mCopyOf() {
        MutableLinkedHashSetWithLinkedElement<Key> set = new MutableLinkedHashSetWithLinkedElement<>(data.listA);
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
    public MutableLinkedHashSetWithLinkedElement<Key> mRemove() {
        MutableLinkedHashSetWithLinkedElement<Key> set = setA.clone();
        for (Key key : data.listA) {
            set.remove(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mRemoveAll() {
        MutableLinkedHashSetWithLinkedElement<Key> set = setA.clone();
        set.removeAll(data.setA);
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mRemoveAllSameType() {
        MutableLinkedHashSetWithLinkedElement<Key> set = setA.clone();
        set.removeAll(setAA);
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mRemoveFirst() {
        var s = setA.clone();
        for (int i = 0, n = setA.size(); i < n; i++) {
            s.removeFirst();
        }
        return s;
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mRetainAllAllRetained() {
        MutableLinkedHashSetWithLinkedElement<Key> set = setA.clone();
        set.retainAll(data.setA);
        assert set.size() == setA.size();
        return set;
    }

    @Benchmark
    public MutableLinkedHashSetWithLinkedElement<Key> mRetainAllNoneRetained() {
        MutableLinkedHashSetWithLinkedElement<Key> set = setA.clone();
        set.retainAll(data.setB);
        assert set.isEmpty();
        return set;
    }


}
