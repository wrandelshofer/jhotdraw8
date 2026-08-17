package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentHashSet;
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
/// # JMH version: 1.36
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
///
/// Benchmark                                (mask)  (size)  Mode  Cnt         Score   Error  Units
/// mRemoveAllFromDifferentType                 -65      10  avgt    2       285.206          ns/op
/// mRemoveAllFromDifferentType                 -65    1000  avgt    2     49381.092          ns/op
/// mRemoveAllFromDifferentType                 -65  100000  avgt    2  12527969.679          ns/op
/// mRemoveAllFromSameType                      -65      10  avgt    2       341.800          ns/op
/// mRemoveAllFromSameType                      -65    1000  avgt    2    116382.908          ns/op
/// mRemoveAllFromSameType                      -65  100000  avgt    2  33739384.456          ns/op
/// mRetainAllFromDifferentTypeAllRetained      -65      10  avgt    2        44.715          ns/op
/// mRetainAllFromDifferentTypeAllRetained      -65    1000  avgt    2      4966.048          ns/op
/// mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt    2    703656.106          ns/op
/// mRetainAllFromDifferentTypeNoneRetained     -65      10  avgt    2       337.661          ns/op
/// mRetainAllFromDifferentTypeNoneRetained     -65    1000  avgt    2     64822.861          ns/op
/// mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt    2  16764453.348          ns/op
/// mRetainAllFromSameTypeAllRetained           -65      10  avgt    2        57.280          ns/op
/// mRetainAllFromSameTypeAllRetained           -65    1000  avgt    2     13358.633          ns/op
/// mRetainAllFromSameTypeAllRetained           -65  100000  avgt    2   2676809.441          ns/op
/// mRetainAllFromSameTypeNoneRetained          -65      10  avgt    2       349.066          ns/op
/// mRetainAllFromSameTypeNoneRetained          -65    1000  avgt    2     69713.732          ns/op
/// mRetainAllFromSameTypeNoneRetained          -65  100000  avgt    2  20339928.915          ns/op
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

    /*
        @Benchmark
        public SimpleImmutableSequencedSet<Key> mCopyOf() {
            SimpleImmutableSequencedSet<Key> set = SimpleImmutableSequencedSet.copyOf(data.listA);
            assert set.size() == data.listA.size();
            return set;
        }


        @Benchmark
        public SimpleImmutableSequencedSet<Key> mCopyOnyByOne() {
            SimpleImmutableSequencedSet<Key> set = SimpleImmutableSequencedSet.of();
            for (Key key : data.listA) {
                set = set.add(key);
            }
            assert set.size() == data.listA.size();
            return set;
        }

        @Benchmark
        public SimpleImmutableSequencedSet<Key> mRemoveOneByOne() {
            SimpleImmutableSequencedSet<Key> set = setA;
            for (Key key : data.listA) {
                set = set.remove(key);
            }
            assert set.isEmpty();
            return set;
        }
    */
    @Benchmark
    public PersistentHashSet<Key> mRemoveAllFromDifferentType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.removingAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRemoveAllFromSameType() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.removingAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public PersistentHashSet<Key> mRetainAllFromDifferentTypeAllRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRetainAllFromDifferentTypeNoneRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public PersistentHashSet<Key> mRetainAllFromSameTypeAllRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(setAA);
        assert updated == setA;
        return updated;
    }


    @Benchmark
    public PersistentHashSet<Key> mRetainAllFromSameTypeNoneRetained() {
        PersistentHashSet<Key> set = setA;
        PersistentHashSet<Key> updated = set.retainingAll(setB);
        assert updated.isEmpty();
        return updated;
    }
/*
    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public SimpleImmutableSequencedSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public SimpleImmutableSequencedSet<Key> mTail() {
        return setA.remove(setA.iterator().next());
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
*/
}
