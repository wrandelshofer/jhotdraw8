package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.ChampVectorSet;
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
 * Benchmark                                (mask)  (size)  Mode  Cnt         Score   Error  Units
 * mContainsFound                              -65  100000  avgt             74.817          ns/op
 * mContainsNotFound                           -65  100000  avgt             61.176          ns/op
 * mCopyOf                                     -65  100000  avgt       29902464.797          ns/op
 * mCopyOnyByOne                               -65  100000  avgt       35587944.904          ns/op
 * mHead                                       -65  100000  avgt             28.361          ns/op
 * mIterate                                    -65  100000  avgt        2081909.162          ns/op
 * mRemoveAllFromDifferentType                 -65  100000  avgt       26291910.722          ns/op
 * mRemoveAllFromSameType                      -65  100000  avgt       76970592.715          ns/op
 * mRemoveOneByOne                             -65  100000  avgt       88791019.823          ns/op
 * mRemoveThenAdd                              -65  100000  avgt            593.484          ns/op
 * mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt        3357320.133          ns/op
 * mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt       32932049.161          ns/op
 * mRetainAllFromSameTypeAllRetained           -65  100000  avgt       10976004.332          ns/op
 * mRetainAllFromSameTypeNoneRetained          -65  100000  avgt       43730938.022          ns/op
 * mTail                                       -65  100000  avgt            144.784          ns/op
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
    private ChampVectorSet<Key> setA;
    private ChampVectorSet<Key> setAA;
    private ChampVectorSet<Key> setB;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ChampVectorSet.copyOf(data.setA);
        setB = ChampVectorSet.copyOf(data.listB);
        setAA = ChampVectorSet.copyOf(data.listA);
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
    public ChampVectorSet<Key> mRemoveAllFromDifferentType() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.removeAll(data.setA);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampVectorSet<Key> mRemoveAllFromSameType() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.removeAll(setAA);
        assert updated.isEmpty();
        return updated;
    }


    @Benchmark
    public ChampVectorSet<Key> mRetainAllFromDifferentTypeAllRetained() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.retainAll(data.setA);
        assert updated == setA;
        return updated;
    }

    @Benchmark
    public ChampVectorSet<Key> mRetainAllFromDifferentTypeNoneRetained() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.retainAll(data.setB);
        assert updated.isEmpty();
        return updated;
    }

    @Benchmark
    public ChampVectorSet<Key> mRetainAllFromSameTypeAllRetained() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.retainAll(setAA);
        assert updated == setA;
        return updated;
    }


    @Benchmark
    public ChampVectorSet<Key> mRetainAllFromSameTypeNoneRetained() {
        ChampVectorSet<Key> set = setA;
        ChampVectorSet<Key> updated = set.retainAll(setB);
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
