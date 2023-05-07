package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.VectorSet;
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

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                             (mask)  (size)  Mode  Cnt          Score   Error  Units
 * mContainsFound                              -65  100000  avgt              62.035          ns/op
 * mContainsNotFound                           -65  100000  avgt              66.128          ns/op
 * mCopyOf                                     -65  100000  avgt        34161362.324          ns/op
 * mCopyOnyByOne                               -65  100000  avgt        34917689.488          ns/op
 * mHead                                       -65  100000  avgt              20.558          ns/op
 * mIterate                                    -65  100000  avgt         2232113.470          ns/op
 * mRemoveAllFromDifferentType                 -65  100000  avgt        32008983.607          ns/op
 * mRemoveAllFromSameType                      -65  100000  avgt       108897936.293          ns/op
 * mRemoveOneByOne                             -65  100000  avgt       100082468.650          ns/op
 * mRemoveThenAdd                              -65  100000  avgt             637.823          ns/op
 * mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt         2938525.017          ns/op
 * mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt        33507643.388          ns/op
 * mRetainAllFromSameTypeAllRetained           -65  100000  avgt        10599907.377          ns/op
 * mRetainAllFromSameTypeNoneRetained          -65  100000  avgt        43232907.194          ns/op
 * mTail                                       -65  100000  avgt             140.800          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorSetJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private VectorSet<Key> setA;
    private VectorSet<Key> setAA;
    private VectorSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = VectorSet.copyOf(data.setA);
        setB = VectorSet.copyOf(data.listB);
        setAA = VectorSet.copyOf(data.listA);
    }

    @Benchmark
    public VectorSet<Key> mCopyOf() {
        VectorSet<Key> set = VectorSet.copyOf(data.listA);
        assert set.size() == data.listA.size();
        return set;
    }


    @Benchmark
    public VectorSet<Key> mCopyOnyByOne() {
        VectorSet<Key> set = VectorSet.of();
        for (Key key : data.listA) {
            set = set.add(key);
        }
        assert set.size() == data.listA.size();
        return set;
    }

    @Benchmark
    public VectorSet<Key> mRemoveOneByOne() {
        VectorSet<Key> set = setA;
        for (Key key : data.listA) {
            set = set.remove(key);
        }
        assert set.isEmpty();
        return set;
    }

    @Benchmark
    public VectorSet<Key> mRemoveAllFromDifferentType() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.removeAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public VectorSet<Key> mRemoveAllFromSameType() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.removeAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public VectorSet<Key> mRetainAllFromDifferentTypeAllRetained() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.retainAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public VectorSet<Key> mRetainAllFromDifferentTypeNoneRetained() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.retainAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public VectorSet<Key> mRetainAllFromSameTypeAllRetained() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.retainAll(setAA);
        assert updated == setA;
        return updated;
    }


    @Benchmark
    public VectorSet<Key> mRetainAllFromSameTypeNoneRetained() {
        VectorSet<Key> set = setA;
        VectorSet<Key> updated = set.retainAll(setB);
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
    public VectorSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public VectorSet<Key> mTail() {
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

}
