package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentLinkedHashSet;
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
/// Benchmark                                        (mask)  (size)  Mode  Cnt         Score   Error  Units
/// PersistentLinkedHashSetJmh.mAdd                     -65      10  avgt    2       342.894          ns/op
/// PersistentLinkedHashSetJmh.mAdd                     -65    1000  avgt    2     79266.675          ns/op
/// PersistentLinkedHashSetJmh.mAdd                     -65  100000  avgt    2  23181899.820          ns/op
/// PersistentLinkedHashSetJmh.mAddAll                  -65      10  avgt    2       207.284          ns/op
/// PersistentLinkedHashSetJmh.mAddAll                  -65    1000  avgt    2     29675.938          ns/op
/// PersistentLinkedHashSetJmh.mAddAll                  -65  100000  avgt    2   7796759.382          ns/op
/// PersistentLinkedHashSetJmh.mAddAllSameType          -65      10  avgt    2       303.973          ns/op
/// PersistentLinkedHashSetJmh.mAddAllSameType          -65    1000  avgt    2     44948.983          ns/op
/// PersistentLinkedHashSetJmh.mAddAllSameType          -65  100000  avgt    2  10212961.696          ns/op
/// PersistentLinkedHashSetJmh.mAddContained            -65      10  avgt    2       109.893          ns/op
/// PersistentLinkedHashSetJmh.mAddContained            -65    1000  avgt    2     33017.892          ns/op
/// PersistentLinkedHashSetJmh.mAddContained            -65  100000  avgt    2   3830700.173          ns/op
/// PersistentLinkedHashSetJmh.mContains                -65      10  avgt    2        21.221          ns/op
/// PersistentLinkedHashSetJmh.mContains                -65    1000  avgt    2      5024.616          ns/op
/// PersistentLinkedHashSetJmh.mContains                -65  100000  avgt    2   2707911.777          ns/op
/// PersistentLinkedHashSetJmh.mContainsAll             -65      10  avgt    2        47.087          ns/op
/// PersistentLinkedHashSetJmh.mContainsAll             -65    1000  avgt    2      8452.297          ns/op
/// PersistentLinkedHashSetJmh.mContainsAll             -65  100000  avgt    2   2719955.784          ns/op
/// PersistentLinkedHashSetJmh.mContainsAllSameType     -65      10  avgt    2       114.914          ns/op
/// PersistentLinkedHashSetJmh.mContainsAllSameType     -65    1000  avgt    2     34457.492          ns/op
/// PersistentLinkedHashSetJmh.mContainsAllSameType     -65  100000  avgt    2   8000243.650          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                  -65      10  avgt    2       245.891          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                  -65    1000  avgt    2     41171.045          ns/op
/// PersistentLinkedHashSetJmh.mCopyOf                  -65  100000  avgt    2   8395270.885          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                -65      10  avgt    2         8.972          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                -65    1000  avgt    2        11.599          ns/op
/// PersistentLinkedHashSetJmh.mGetFirst                -65  100000  avgt    2        12.670          ns/op
/// PersistentLinkedHashSetJmh.mIterate                 -65      10  avgt    2       107.162          ns/op
/// PersistentLinkedHashSetJmh.mIterate                 -65    1000  avgt    2     28889.680          ns/op
/// PersistentLinkedHashSetJmh.mIterate                 -65  100000  avgt    2   6304511.916          ns/op
/// PersistentLinkedHashSetJmh.mRemove                  -65      10  avgt    2       392.403          ns/op
/// PersistentLinkedHashSetJmh.mRemove                  -65    1000  avgt    2    131201.573          ns/op
/// PersistentLinkedHashSetJmh.mRemove                  -65  100000  avgt    2  41785576.769          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll               -65      10  avgt    2       250.573          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll               -65    1000  avgt    2     75191.933          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAll               -65  100000  avgt    2  14102922.477          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType       -65      10  avgt    2       376.571          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType       -65    1000  avgt    2     92846.731          ns/op
/// PersistentLinkedHashSetJmh.mRemoveAllSameType       -65  100000  avgt    2  17918570.538          ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst             -65      10  avgt    2       393.773          ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst             -65    1000  avgt    2    114575.544          ns/op
/// PersistentLinkedHashSetJmh.mRemoveFirst             -65  100000  avgt    2  23273662.727          ns/op
/// PersistentLinkedHashSetJmh.mRetainAll               -65      10  avgt    2       512.296          ns/op
/// PersistentLinkedHashSetJmh.mRetainAll               -65    1000  avgt    2     96822.671          ns/op
/// PersistentLinkedHashSetJmh.mRetainAll               -65  100000  avgt    2  15845390.178          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllSameType       -65      10  avgt    2       675.682          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllSameType       -65    1000  avgt    2    103724.478          ns/op
/// PersistentLinkedHashSetJmh.mRetainAllSameType       -65  100000  avgt    2  17394543.073          ns/op       ns/op        ns/op
/// ```
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
    private PersistentLinkedHashSet<Key> setC;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentLinkedHashSet.copyOf(data.setA);
        setAA = PersistentLinkedHashSet.copyOf(data.listA);
        setC = PersistentLinkedHashSet.copyOf(data.listC);
    }

    @Benchmark
    public PersistentSet<Key> mAdd() {
        PersistentLinkedHashSet<Key> set = PersistentLinkedHashSet.of();
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mAddAll() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.addingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mAddAllSameType() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.addingAll(setC);
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
        PersistentSet<Key> set = PersistentLinkedHashSet.copyOf(data.listA);
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
    public PersistentLinkedHashSet<Key> mRemoveAll() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.removingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRemoveAllSameType() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.removingAll(setC);
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
    public PersistentLinkedHashSet<Key> mRetainAll() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.retainingAll(data.setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

    @Benchmark
    public PersistentLinkedHashSet<Key> mRetainAllSameType() {
        PersistentLinkedHashSet<Key> set = setA;
        PersistentLinkedHashSet<Key> updated = set.retainingAll(setC);
        assert updated.size() == data.listC.size() / 2;
        return updated;
    }

}
