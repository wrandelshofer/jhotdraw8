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
/// Benchmark                                  (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentHashSetJmh.mAdd                     -65      10  avgt    2       145.428          ns/op
/// PersistentHashSetJmh.mAdd                     -65    1000  avgt    2     29111.573          ns/op
/// PersistentHashSetJmh.mAdd                     -65  100000  avgt    2  11178605.471          ns/op
/// PersistentHashSetJmh.mAddAll                  -65      10  avgt    2       120.493          ns/op
/// PersistentHashSetJmh.mAddAll                  -65    1000  avgt    2     24336.473          ns/op
/// PersistentHashSetJmh.mAddAll                  -65  100000  avgt    2   6895371.626          ns/op
/// PersistentHashSetJmh.mAddAllSameType          -65      10  avgt    2        74.240          ns/op
/// PersistentHashSetJmh.mAddAllSameType          -65    1000  avgt    2      9918.191          ns/op
/// PersistentHashSetJmh.mAddAllSameType          -65  100000  avgt    2   1212284.483          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType       -65      10  avgt    2        27.586          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType       -65    1000  avgt    2      9163.258          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType       -65  100000  avgt    2   1099994.606          ns/op
/// PersistentHashSetJmh.mRetainAllSameType       -65      10  avgt    2        22.885          ns/op
/// PersistentHashSetJmh.mRetainAllSameType       -65    1000  avgt    2      7032.122          ns/op
/// PersistentHashSetJmh.mRetainAllSameType       -65  100000  avgt    2    745823.683          ns/op
/// PersistentHashSetJmh.mAddContained            -65      10  avgt    2        14.624          ns/op
/// PersistentHashSetJmh.mAddContained            -65    1000  avgt    2      6248.901          ns/op
/// PersistentHashSetJmh.mAddContained            -65  100000  avgt    2   1876059.206          ns/op
/// PersistentHashSetJmh.mContains                -65      10  avgt    2        19.840          ns/op
/// PersistentHashSetJmh.mContains                -65    1000  avgt    2      4643.832          ns/op
/// PersistentHashSetJmh.mContains                -65  100000  avgt    2   2127054.029          ns/op
/// PersistentHashSetJmh.mContainsAll             -65      10  avgt    2        45.736          ns/op
/// PersistentHashSetJmh.mContainsAll             -65    1000  avgt    2      7657.374          ns/op
/// PersistentHashSetJmh.mContainsAll             -65  100000  avgt    2   2708017.965          ns/op
/// PersistentHashSetJmh.mContainsAllSameType     -65      10  avgt    2        51.166          ns/op
/// PersistentHashSetJmh.mContainsAllSameType     -65    1000  avgt    2      9691.541          ns/op
/// PersistentHashSetJmh.mContainsAllSameType     -65  100000  avgt    2   1645741.333          ns/op
/// PersistentHashSetJmh.mCopyOf                  -65      10  avgt    2       236.735          ns/op
/// PersistentHashSetJmh.mCopyOf                  -65    1000  avgt    2     23217.806          ns/op
/// PersistentHashSetJmh.mCopyOf                  -65  100000  avgt    2   6477167.848          ns/op
/// PersistentHashSetJmh.mGetFirst                -65      10  avgt    2         0.811          ns/op
/// PersistentHashSetJmh.mGetFirst                -65    1000  avgt    2        40.029          ns/op
/// PersistentHashSetJmh.mIterate                 -65      10  avgt    2        25.868          ns/op
/// PersistentHashSetJmh.mIterate                 -65    1000  avgt    2      3500.900          ns/op
/// PersistentHashSetJmh.mIterate                 -65  100000  avgt    2    771528.138          ns/op
/// PersistentHashSetJmh.mRemove                  -65      10  avgt    2       136.326          ns/op
/// PersistentHashSetJmh.mRemove                  -65    1000  avgt    2     33904.461          ns/op
/// PersistentHashSetJmh.mRemove                  -65  100000  avgt    2  10655569.448          ns/op
/// PersistentHashSetJmh.mRemoveAll               -65      10  avgt    2        96.066          ns/op
/// PersistentHashSetJmh.mRemoveAll               -65    1000  avgt    2     25737.980          ns/op
/// PersistentHashSetJmh.mRemoveAll               -65  100000  avgt    2   8209116.574          ns/op
/// PersistentHashSetJmh.mRemoveAllSameType       -65      10  avgt    2        77.407          ns/op
/// PersistentHashSetJmh.mRemoveFirst             -65      10  avgt    2         6.262          ns/op
/// PersistentHashSetJmh.mRemoveFirst             -65    1000  avgt    2        52.761          ns/op
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
    private PersistentHashSet<Key> setC;
    private PersistentHashSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentHashSet.copyOf(data.setA);
        setAA = PersistentHashSet.copyOf(data.listA);
        setB = PersistentHashSet.copyOf(data.listB);
        setC = PersistentHashSet.copyOf(data.listC);
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
    public PersistentHashSet<Key> mAddAll() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.addingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mAddAllSameType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.addingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
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
    public int mContains() {
        int count = 0;
        for (Key k : data.listC) {
            if (setA.contains(k)) count++;
        }
        assert count == data.listC.size() / 2;
        return count;
    }

    @Benchmark
    public boolean mContainsAll() {
        return setA.containsAll(data.setA);
    }

    @Benchmark
    public boolean mContainsAllSameType() {
        return setA.containsAll(setAA);
    }

    @Benchmark
    public PersistentSet<Key> mCopyOf() {
        PersistentSet<Key> set = PersistentHashSet.copyOf(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public Key mGetFirst() {
        return setA.iterator().next();
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
        PersistentHashSet<Key> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRemoveAllSameType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.removingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentSet<Key> mRemoveFirst() {
        PersistentSet<Key> set = setA;
        while (!set.isEmpty()) {
            set = set.removing(set.iterator().next());
        }
        return set;
    }

    @Benchmark
    public PersistentHashSet<Key> mRetainAll() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRetainAllSameType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }


}
