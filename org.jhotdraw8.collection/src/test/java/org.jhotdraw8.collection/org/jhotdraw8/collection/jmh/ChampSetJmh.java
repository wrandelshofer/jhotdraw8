package org.jhotdraw8.collection.jmh;


import org.jhotdraw8.collection.ChampSet;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
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
 * Benchmark                                            (mask)  (size)  Mode  Cnt         Score   Error  Units
 * ChampSetJmh.mContainsFound                              -65  100000  avgt            52.576          ns/op
 * ChampSetJmh.mContainsNotFound                           -65  100000  avgt            60.258          ns/op
 * ChampSetJmh.mCopyOf                                     -65  100000  avgt      14986801.283          ns/op
 * ChampSetJmh.mCopyOnyByOne                               -65  100000  avgt      23692842.288          ns/op
 * ChampSetJmh.mHead                                       -65  100000  avgt            55.770          ns/op
 * ChampSetJmh.mIterate                                    -65  100000  avgt    4  1592366.817 ± 92156.829  ns/op
 * ChampSetJmh.mIterateEnumerator                          -65  100000  avgt    4  1203903.174 ± 67186.841  ns/op
 * ChampSetJmh.mRemoveAllFromDifferentType                 -65  100000  avgt      17936675.235          ns/op
 * ChampSetJmh.mRemoveAllFromSameType                      -65  100000  avgt       1363740.078          ns/op
 * ChampSetJmh.mRemoveOneByOne                             -65  100000  avgt      25836920.216          ns/op
 * ChampSetJmh.mRemoveThenAdd                              -65  100000  avgt           348.779          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeAllRetained      -65  100000  avgt       5828475.459          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65  100000  avgt       4298284.677          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeAllRetained           -65  100000  avgt       1525086.025          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeNoneRetained          -65  100000  avgt       1452778.074          ns/op
 * ChampSetJmh.mTail                                       -65  100000  avgt           141.728          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ChampSetJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private ChampSet<Key> setA;
    private ChampSet<Key> setB;
    private ChampSet<Key> setAA;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        setA = ChampSet.copyOf(data.setA);
        setB = ChampSet.copyOf(data.listB);
        setAA = ChampSet.copyOf(data.listA);
        assert setA.size() == size;
        assert setB.size() == size;
        assert setAA.size() == size;
    }

    /*
        @Benchmark
        public ChampSet<Key> mCopyOf() {
            ChampSet<Key> set = ChampSet.copyOf(data.listA);
            assert set.size() == data.listA.size();
            return set;
        }


        @Benchmark
        public ChampSet<Key> mCopyOnyByOne() {
            ChampSet<Key> set = ChampSet.of();
            for (Key key : data.listA) {
                set = set.add(key);
            }
            assert set.size() == data.listA.size();
            return set;
        }

        @Benchmark
        public ChampSet<Key> mRemoveOneByOne() {
            ChampSet<Key> set = setA;
            for (Key key : data.listA) {
                set = set.remove(key);
            }
            assert set.isEmpty();
            return set;
        }

        @Benchmark
        public ChampSet<Key> mRemoveAllFromDifferentType() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.removeAll(data.setA);
            assert updated.isEmpty();
            return updated;
        }

        @Benchmark
        public ChampSet<Key> mRemoveAllFromSameType() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.removeAll(setAA);
            assert updated.isEmpty();
            return updated;
        }


        @Benchmark
        public ChampSet<Key> mRetainAllFromDifferentTypeAllRetained() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.retainAll(data.setA);
            assert updated == setA;
            return updated;
        }

        @Benchmark
        public ChampSet<Key> mRetainAllFromDifferentTypeNoneRetained() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.retainAll(data.setB);
            assert updated.isEmpty();
            return updated;
        }

        @Benchmark
        public ChampSet<Key> mRetainAllFromSameTypeAllRetained() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.retainAll(setAA);
            assert updated == setA;
            return updated;
        }


        @Benchmark
        public ChampSet<Key> mRetainAllFromSameTypeNoneRetained() {
            ChampSet<Key> set = setA;
            ChampSet<Key> updated = set.retainAll(setB);
            assert updated.isEmpty();
            return updated;
        }
    */
    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Key k : setA) {
            sum += k.value;
        }
        return sum;
    }

    @Benchmark
    public int mIterateEnumerator() {
        int sum = 0;
        for (EnumeratorSpliterator<Key> i = setA.spliterator(); i.moveNext(); ) {
            sum += i.current().value;
        }
        return sum;
    }
/*
    @Benchmark
    public ChampSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
    }

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }

    @Benchmark
    public ChampSet<Key> mTail() {
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

/*
    public static void main(String... args){
        ChampSetJmh jmh = new ChampSetJmh();
        jmh.size=70;
        jmh.mask=-65;
        jmh.setup();
        System.out.println(jmh.mRetainAllFromSameTypeAllRetained().size());
    }

 */
}
