package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentLinkedHashElementSet;
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
/// Benchmark                                      (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentLinkedSetJmh.mAdd                       -65      10  avgt    2       295.958          ns/op
/// PersistentLinkedSetJmh.mAdd                       -65    1000  avgt    2     80651.388          ns/op
/// PersistentLinkedSetJmh.mAdd                       -65  100000  avgt    2  20323629.149          ns/op
/// PersistentLinkedSetJmh.mContainsFound             -65      10  avgt    2        39.470          ns/op
/// PersistentLinkedSetJmh.mContainsFound             -65    1000  avgt    2      8741.645          ns/op
/// PersistentLinkedSetJmh.mContainsFound             -65  100000  avgt    2   2002041.414          ns/op
/// PersistentLinkedSetJmh.mContainsNotFound          -65      10  avgt    2        24.933          ns/op
/// PersistentLinkedSetJmh.mContainsNotFound          -65    1000  avgt    2      6077.630          ns/op
/// PersistentLinkedSetJmh.mContainsNotFound          -65  100000  avgt    2   1528774.587          ns/op
/// PersistentLinkedSetJmh.mCopyOf                    -65      10  avgt    2       176.845          ns/op
/// PersistentLinkedSetJmh.mCopyOf                    -65    1000  avgt    2     32482.363          ns/op
/// PersistentLinkedSetJmh.mCopyOf                    -65  100000  avgt    2   8877872.467          ns/op
/// PersistentLinkedSetJmh.mGetFirst                  -65      10  avgt    2         5.557          ns/op
/// PersistentLinkedSetJmh.mGetFirst                  -65    1000  avgt    2         6.246          ns/op
/// PersistentLinkedSetJmh.mGetFirst                  -65  100000  avgt    2         7.480          ns/op
/// PersistentLinkedSetJmh.mIterate                   -65      10  avgt    2        81.640          ns/op
/// PersistentLinkedSetJmh.mIterate                   -65    1000  avgt    2     28680.694          ns/op
/// PersistentLinkedSetJmh.mIterate                   -65  100000  avgt    2   5774263.695          ns/op
/// PersistentLinkedSetJmh.mRemove                    -65      10  avgt    2       462.118          ns/op
/// PersistentLinkedSetJmh.mRemove                    -65    1000  avgt    2    189397.843          ns/op
/// PersistentLinkedSetJmh.mRemove                    -65  100000  avgt    2  55636421.297          ns/op
/// PersistentLinkedSetJmh.mRemoveAll                 -65      10  avgt    2       562.079          ns/op
/// PersistentLinkedSetJmh.mRemoveAll                 -65    1000  avgt    2    114168.157          ns/op
/// PersistentLinkedSetJmh.mRemoveAll                 -65  100000  avgt    2  26979907.346          ns/op
/// PersistentLinkedSetJmh.mRemoveAllSameType         -65      10  avgt    2       605.373          ns/op
/// PersistentLinkedSetJmh.mRemoveAllSameType         -65    1000  avgt    2    130130.044          ns/op
/// PersistentLinkedSetJmh.mRemoveAllSameType         -65  100000  avgt    2  39018346.383          ns/op
/// PersistentLinkedSetJmh.mRemoveFirst               -65      10  avgt    2       461.719          ns/op
/// PersistentLinkedSetJmh.mRemoveFirst               -65    1000  avgt    2    137037.369          ns/op
/// PersistentLinkedSetJmh.mRemoveFirst               -65  100000  avgt    2  26075829.258          ns/op
/// PersistentLinkedSetJmh.mRetainAllAllRetained      -65      10  avgt    2       394.456          ns/op
/// PersistentLinkedSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     82741.858          ns/op
/// PersistentLinkedSetJmh.mRetainAllAllRetained      -65  100000  avgt    2  17437576.303          ns/op
/// PersistentLinkedSetJmh.mRetainAllNoneRetained     -65      10  avgt    2       875.497          ns/op
/// PersistentLinkedSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2    185253.215          ns/op
/// PersistentLinkedSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  43977050.717          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentLinkedHashElementSetJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentLinkedHashElementSet<Key> setA;
    private PersistentLinkedHashElementSet<Key> setAA;
    private PersistentLinkedHashElementSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentLinkedHashElementSet.copyOf(data.setA);
        setB = PersistentLinkedHashElementSet.copyOf(data.listB);
        setAA = PersistentLinkedHashElementSet.copyOf(data.listA);
    }

    /*
        @Benchmark
        public PersistentLinkedSet<Key> mCopyOf() {
            PersistentLinkedSet<Key> set = PersistentLinkedSet.copyOf(data.listA);
            assert set.size() == data.listA.size();
            return set;
        }


        @Benchmark
        public PersistentLinkedSet<Key> mAdd() {
            PersistentLinkedSet<Key> set = PersistentLinkedSet.of();
            for (Key key : data.listA) {
                set = set.adding(key);
            }
            assert set.size() == data.listA.size();
            return set;
        }

        @Benchmark
        public PersistentLinkedSet<Key> mRemove() {
            PersistentLinkedSet<Key> set = setA;
            for (Key key : data.listA) {
                set = set.removing(key);
            }
            assert set.isEmpty();
            return set;
        }

        @Benchmark
        public PersistentLinkedSet<Key> mRemoveAll() {
            PersistentLinkedSet<Key> set = setA;
            PersistentLinkedSet<Key> updated = set.removingAll(data.setA);
            assert updated.isEmpty();
            return updated;
        }

        @Benchmark
        public PersistentLinkedSet<Key> mRemoveAllSameType() {
            PersistentLinkedSet<Key> set = setA;
            PersistentLinkedSet<Key> updated = set.removingAll(setAA);
            assert updated.isEmpty();
            return updated;
        }


        @Benchmark
        public PersistentLinkedSet<Key> mRetainAllAllRetained() {
            PersistentLinkedSet<Key> set = setA;
            PersistentLinkedSet<Key> updated = set.retainingAll(data.setA);
            assert updated == setA;
            return updated;
        }

        @Benchmark
        public PersistentLinkedSet<Key> mRetainAllNoneRetained() {
            PersistentLinkedSet<Key> set = setA;
            PersistentLinkedSet<Key> updated = set.retainingAll(data.setB);
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
    */
    @Benchmark
    public PersistentLinkedHashElementSet<Key> mRemoveFirst() {
        var s = setA;
        for (int i = 0, n = setA.size(); i < n; i++) {
            s = s.removingFirst();
        }
        return s;
    }
/*
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
    */
}
