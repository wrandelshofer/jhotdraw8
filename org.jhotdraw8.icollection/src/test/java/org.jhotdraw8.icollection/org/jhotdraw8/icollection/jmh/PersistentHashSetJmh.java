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

/// ```
/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark             (mask)  (size)  Mode  Cnt         Score   Error  Units
/// mAdd                     -65      10  avgt    2       128.071          ns/op
/// mAdd                     -65    1000  avgt    2     30035.914          ns/op
/// mAdd                     -65  100000  avgt    2  10027624.480          ns/op
/// mAddAll                  -65      10  avgt    2       110.992          ns/op
/// mAddAll                  -65    1000  avgt    2     26631.509          ns/op
/// mAddAll                  -65  100000  avgt    2   7518205.627          ns/op
/// mAddAllSameType          -65      10  avgt    2        81.381          ns/op
/// mAddAllSameType          -65    1000  avgt    2      9589.576          ns/op
/// mAddAllSameType          -65  100000  avgt    2   1219409.850          ns/op
/// mAddContained            -65      10  avgt    2        26.688          ns/op
/// mAddContained            -65    1000  avgt    2      6774.541          ns/op
/// mAddContained            -65  100000  avgt    2   1992140.126          ns/op
/// mContains                -65      10  avgt    2        19.856          ns/op
/// mContains                -65    1000  avgt    2      5167.463          ns/op
/// mContains                -65  100000  avgt    2   2232320.875          ns/op
/// mContainsAll             -65      10  avgt    2        45.614          ns/op
/// mContainsAll             -65    1000  avgt    2      8473.466          ns/op
/// mContainsAll             -65  100000  avgt    2   2838307.035          ns/op
/// mContainsAllSameType     -65      10  avgt    2        54.893          ns/op
/// mContainsAllSameType     -65    1000  avgt    2     10504.696          ns/op
/// mContainsAllSameType     -65  100000  avgt    2   1678911.115          ns/op
/// mCopyOf                  -65      10  avgt    2       233.209          ns/op
/// mCopyOf                  -65    1000  avgt    2     24771.853          ns/op
/// mCopyOf                  -65  100000  avgt    2   6546373.709          ns/op
/// mGetFirst                -65      10  avgt    2         1.091          ns/op
/// mGetFirst                -65    1000  avgt    2         9.019          ns/op
/// mGetFirst                -65  100000  avgt    2        12.632          ns/op
/// mIterate                 -65      10  avgt    2        25.017          ns/op
/// mIterate                 -65    1000  avgt    2      3706.868          ns/op
/// mIterate                 -65  100000  avgt    2    698077.224          ns/op
/// mRemove                  -65      10  avgt    2       138.045          ns/op
/// mRemove                  -65    1000  avgt    2     36136.914          ns/op
/// mRemove                  -65  100000  avgt    2  10812175.640          ns/op
/// mRemoveAll               -65      10  avgt    2        96.604          ns/op
/// mRemoveAll               -65    1000  avgt    2     24331.176          ns/op
/// mRemoveAll               -65  100000  avgt    2   8366146.704          ns/op
/// mRemoveAllSameType       -65      10  avgt    2        27.005          ns/op
/// mRemoveAllSameType       -65    1000  avgt    2      9622.622          ns/op
/// mRemoveAllSameType       -65  100000  avgt    2   1090335.965          ns/op
/// mRemoveFirst             -65      10  avgt    2       192.411          ns/op
/// mRemoveFirst             -65    1000  avgt    2     53704.964          ns/op
/// mRemoveFirst             -65  100000  avgt    2   8595315.303          ns/op
/// mRetainAll               -65      10  avgt    2       138.870          ns/op
/// mRetainAll               -65    1000  avgt    2     23391.418          ns/op
/// mRetainAll               -65  100000  avgt    2   4891726.496          ns/op
/// mRetainAllSameType       -65      10  avgt    2        23.106          ns/op
/// mRetainAllSameType       -65    1000  avgt    2      7185.787          ns/op
/// mRetainAllSameType       -65  100000  avgt    2    737232.599          ns/op
/// ```
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

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentHashSet.copyOf(data.setA);
        setAA = PersistentHashSet.copyOf(data.listA);
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
