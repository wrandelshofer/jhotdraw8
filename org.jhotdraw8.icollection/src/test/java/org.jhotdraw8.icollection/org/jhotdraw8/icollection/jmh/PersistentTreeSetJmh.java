package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentTreeSet;
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
/// # VM version: JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
/// </pre>
/// <pre>
/// without specialized retainAll method
///
/// Benchmark                                               (mask)  (size)  Mode  Cnt         Score   Error  Units
/// RedBlackSetJmh.mContainsFound                              -65  100000  avgt    2       259.198          ns/op
/// RedBlackSetJmh.mContainsNotFound                           -65  100000  avgt    2       255.548          ns/op
/// RedBlackSetJmh.mCopyOf                                     -65  100000  avgt    2  51774003.130          ns/op
/// RedBlackSetJmh.mCopyOnyByOne                               -65  100000  avgt    2  51899354.844          ns/op
/// RedBlackSetJmh.mHead                                       -65  100000  avgt    2        97.218          ns/op
/// RedBlackSetJmh.mIterate                                    -65  100000  avgt    2   1324875.033          ns/op
/// RedBlackSetJmh.mRemoveAllFromDifferentType                 -65  100000  avgt    2  62601911.900          ns/op
/// RedBlackSetJmh.mRemoveAllFromSameType                      -65  100000  avgt    2  29508201.168          ns/op
/// RedBlackSetJmh.mRemoveOneByOne                             -65  100000  avgt    2  65430328.871          ns/op
/// RedBlackSetJmh.mRemoveThenAdd                              -65  100000  avgt    2       980.659          ns/op
/// RedBlackSetJmh.mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt    2   4899701.739          ns/op
/// RedBlackSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt    2  36681930.828          ns/op
/// RedBlackSetJmh.mRetainAllFromSameTypeAllRetained           -65  100000  avgt    2  13563797.557          ns/op
/// RedBlackSetJmh.mRetainAllFromSameTypeNoneRetained          -65  100000  avgt    2  41174937.247          ns/op
/// RedBlackSetJmh.mTail                                       -65  100000  avgt    2       309.179          ns/op
/// </pre>
/// <pre>
/// with speciaized retainAll method
///
/// Benchmark                                               (mask)  (size)  Mode  Cnt         Score   Error  Units
/// RedBlackSetJmh.mContainsFound                              -65  100000  avgt    2       374.532          ns/op
/// RedBlackSetJmh.mContainsNotFound                           -65  100000  avgt    2       294.506          ns/op
/// RedBlackSetJmh.mCopyOf                                     -65  100000  avgt    2  59756744.232          ns/op
/// RedBlackSetJmh.mCopyOnyByOne                               -65  100000  avgt    2  61715736.079          ns/op
/// RedBlackSetJmh.mHead                                       -65  100000  avgt    2        93.888          ns/op
/// RedBlackSetJmh.mIterate                                    -65  100000  avgt    2   1600415.669          ns/op
/// RedBlackSetJmh.mRemoveAllFromDifferentType                 -65  100000  avgt    2  70145994.171          ns/op
/// RedBlackSetJmh.mRemoveAllFromSameType                      -65  100000  avgt    2  21855256.756          ns/op
/// RedBlackSetJmh.mRemoveOneByOne                             -65  100000  avgt    2  69026783.455          ns/op
/// RedBlackSetJmh.mRemoveThenAdd                              -65  100000  avgt    2       928.906          ns/op
/// RedBlackSetJmh.mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt    2   7127755.303          ns/op
/// RedBlackSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt    2  30458760.679          ns/op
/// RedBlackSetJmh.mRetainAllFromSameTypeAllRetained           -65  100000  avgt    2  10184702.314          ns/op
/// RedBlackSetJmh.mRetainAllFromSameTypeNoneRetained          -65  100000  avgt    2  17160108.223          ns/op
/// RedBlackSetJmh.mTail                                       -65  100000  avgt    2       295.686          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xmx15g",})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PersistentTreeSetJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private PersistentTreeSet<Key> setA;
    private PersistentTreeSet<Key> setB;
    private PersistentTreeSet<Key> setAA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = PersistentTreeSet.copyOf(data.setA);
        setB = PersistentTreeSet.copyOf(data.listB);
        setAA = PersistentTreeSet.copyOf(data.listA);
        assert setA.size() == size;
        assert setB.size() == size;
        assert setAA.size() == size;
    }


    @Benchmark
    public PersistentTreeSet<Key> mCopyOf() {
        PersistentTreeSet<Key> set = PersistentTreeSet.copyOf(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public PersistentTreeSet<Key> mCopyOnyByOne() {
        PersistentTreeSet<Key> set = PersistentTreeSet.of();
        for (Key key : data.listA) {
            set = set.adding(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public PersistentTreeSet<Key> mRemoveOneByOne() {
        PersistentTreeSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.removing(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public PersistentTreeSet<Key> mRemoveAllFromDifferentType() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentTreeSet<Key> mRemoveAllFromSameType() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentTreeSet<Key> mRetainAllFromDifferentTypeAllRetained() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentTreeSet<Key> mRetainAllFromDifferentTypeNoneRetained() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentTreeSet<Key> mRetainAllFromSameTypeAllRetained() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.retainingAll(setAA);
        assert updated == setA;
        return updated;
    }


    @Benchmark
    public PersistentTreeSet<Key> mRetainAllFromSameTypeNoneRetained() {
        PersistentTreeSet<Key> set = setA;
        PersistentTreeSet<Key> updated = set.retainingAll(setB);
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
    public Key mHead() {
        return setA.iterator().next();
    }


    @Benchmark
    public PersistentTreeSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.removing(key).adding(key);
    }


    @Benchmark
    public PersistentTreeSet<Key> mTail() {
        return setA.removing(setA.iterator().next());
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return setA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return setA.contains(key);
    }

}
