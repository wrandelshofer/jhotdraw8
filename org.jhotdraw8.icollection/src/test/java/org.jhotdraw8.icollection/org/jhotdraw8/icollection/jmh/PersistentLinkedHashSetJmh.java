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
///
/// Benchmark                                          (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentLinkedHashSetJmh.mAdd                       -65      10  avgt    2       366.794          ns/op
/// PersistentLinkedHashSetJmh.mAdd                       -65    1000  avgt    2     77870.971          ns/op
/// PersistentLinkedHashSetJmh.mAdd                       -65  100000  avgt    2  20964397.433          ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65      10  avgt    2        57.588   *      ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65    1000  avgt    2     32807.924   *      ns/op
/// PersistentLinkedHashSetJmh.mAddContained              -65  100000  avgt    2   3826495.979   *      ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65      10  avgt    2        22.505          ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65    1000  avgt    2      6050.271          ns/op
/// PersistentLinkedHashSetJmh.mContainsFound             -65  100000  avgt    2   1924379.054          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65      10  avgt    2         9.725          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65    1000  avgt    2      4482.954          ns/op
/// PersistentLinkedHashSetJmh.mContainsNotFound          -65  100000  avgt    2   1672341.356          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65      10  avgt    2       254.167          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65    1000  avgt    2     42279.369          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                    -65  100000  avgt    2   8270214.334   *      ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65      10  avgt    2         0.763          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65    1000  avgt    2         0.788          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                  -65  100000  avgt    2         0.786          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65      10  avgt    2       105.641          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65    1000  avgt    2     27805.804          ns/op
/// PersistentLinkedHashSetJmh.mIterate                   -65  100000  avgt    2   6179073.977          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65      10  avgt    2       389.526          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65    1000  avgt    2    131780.005          ns/op
/// PersistentLinkedHashSetJmh.mRemove                    -65  100000  avgt    2  38864000.808          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65      10  avgt    2       329.974          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65    1000  avgt    2    106485.436          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll                 -65  100000  avgt    2  16934785.037          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65      10  avgt    2       570.905          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65    1000  avgt    2    156796.566          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType         -65  100000  avgt    2  25093804.242   *      ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst               -65      10  avgt    2       311.399          ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst               -65    1000  avgt    2     87211.136          ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst               -65  100000  avgt    2  22360576.868          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65      10  avgt    2        98.480          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65    1000  avgt    2     27760.201          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllAllRetained      -65  100000  avgt    2   6656120.009          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65      10  avgt    2       550.856          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65    1000  avgt    2    123203.328          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllNoneRetained     -65  100000  avgt    2  18364972.401          ns/op        ns/op
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
    public PersistentLinkedHashSet<Key> mAddContained() {
        PersistentLinkedHashSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

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
