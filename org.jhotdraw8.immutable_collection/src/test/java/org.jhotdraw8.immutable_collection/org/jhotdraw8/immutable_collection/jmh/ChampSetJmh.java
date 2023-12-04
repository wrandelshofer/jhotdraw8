package org.jhotdraw8.immutable_collection.jmh;


import org.jhotdraw8.immutable_collection.ChampSet;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * Benchmark                          (mask)  (size)  Mode  Cnt        Score   Error  Units
 * ChampSetJmh.mIterate                  -65  100000  avgt        844158.536          ns/op
 * ChampSetJmh.mIterateEnumerator        -65  100000  avgt       1244576.380          ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                            (mask)    (size)  Mode  Cnt           Score   Error  Units
 * ChampSetJmh.mContainsFound                              -65        10  avgt                6.531          ns/op
 * ChampSetJmh.mContainsFound                              -65      1000  avgt               19.339          ns/op
 * ChampSetJmh.mContainsFound                              -65    100000  avgt               47.582          ns/op
 * ChampSetJmh.mContainsFound                              -65  10000000  avgt              254.669          ns/op
 * ChampSetJmh.mContainsNotFound                           -65        10  avgt                6.406          ns/op
 * ChampSetJmh.mContainsNotFound                           -65      1000  avgt               18.927          ns/op
 * ChampSetJmh.mContainsNotFound                           -65    100000  avgt               46.014          ns/op
 * ChampSetJmh.mContainsNotFound                           -65  10000000  avgt              254.937          ns/op
 * ChampSetJmh.mCopyOf                                     -65        10  avgt              317.836          ns/op
 * ChampSetJmh.mCopyOf                                     -65      1000  avgt            74404.258          ns/op
 * ChampSetJmh.mCopyOf                                     -65    100000  avgt         14735906.381          ns/op
 * ChampSetJmh.mCopyOf                                     -65  10000000  avgt       4359554195.667          ns/op
 * ChampSetJmh.mCopyOnyByOne                               -65        10  avgt              378.281          ns/op
 * ChampSetJmh.mCopyOnyByOne                               -65      1000  avgt            97965.052          ns/op
 * ChampSetJmh.mCopyOnyByOne                               -65    100000  avgt         21545690.991          ns/op
 * ChampSetJmh.mCopyOnyByOne                               -65  10000000  avgt       6506215978.500          ns/op
 * ChampSetJmh.mHead                                       -65        10  avgt                9.537          ns/op
 * ChampSetJmh.mHead                                       -65      1000  avgt               11.642          ns/op
 * ChampSetJmh.mHead                                       -65    100000  avgt               21.940          ns/op
 * ChampSetJmh.mHead                                       -65  10000000  avgt               27.751          ns/op
 * ChampSetJmh.mIterate                                    -65        10  avgt               17.048          ns/op
 * ChampSetJmh.mIterate                                    -65      1000  avgt             3685.048          ns/op
 * ChampSetJmh.mIterate                                    -65    100000  avgt           703833.573          ns/op
 * ChampSetJmh.mIterate                                    -65  10000000  avgt        286645121.114          ns/op
 * ChampSetJmh.mIterateEnumerator                          -65        10  avgt               15.061          ns/op
 * ChampSetJmh.mIterateEnumerator                          -65      1000  avgt             5328.946          ns/op
 * ChampSetJmh.mIterateEnumerator                          -65    100000  avgt          1153913.409          ns/op
 * ChampSetJmh.mIterateEnumerator                          -65  10000000  avgt        355126769.310          ns/op
 * ChampSetJmh.mIterateEnumeratorOld                       -65        10  avgt               19.619          ns/op
 * ChampSetJmh.mIterateEnumeratorOld                       -65      1000  avgt             6437.539          ns/op
 * ChampSetJmh.mIterateEnumeratorOld                       -65    100000  avgt          1246307.863          ns/op
 * ChampSetJmh.mIterateEnumeratorOld                       -65  10000000  avgt        385883555.654          ns/op
 * ChampSetJmh.mRemoveAllFromDifferentType                 -65        10  avgt              348.410          ns/op
 * ChampSetJmh.mRemoveAllFromDifferentType                 -65      1000  avgt            90834.499          ns/op
 * ChampSetJmh.mRemoveAllFromDifferentType                 -65    100000  avgt         16550150.296          ns/op
 * ChampSetJmh.mRemoveAllFromDifferentType                 -65  10000000  avgt       3790235960.000          ns/op
 * ChampSetJmh.mRemoveAllFromSameType                      -65        10  avgt               80.781          ns/op
 * ChampSetJmh.mRemoveAllFromSameType                      -65      1000  avgt            11948.630          ns/op
 * ChampSetJmh.mRemoveAllFromSameType                      -65    100000  avgt          1400446.241          ns/op
 * ChampSetJmh.mRemoveAllFromSameType                      -65  10000000  avgt        247237967.220          ns/op
 * ChampSetJmh.mRemoveOneByOne                             -65        10  avgt              294.905          ns/op
 * ChampSetJmh.mRemoveOneByOne                             -65      1000  avgt           105479.850          ns/op
 * ChampSetJmh.mRemoveOneByOne                             -65    100000  avgt         23467126.398          ns/op
 * ChampSetJmh.mRemoveOneByOne                             -65  10000000  avgt       6457404845.500          ns/op
 * ChampSetJmh.mRemoveThenAdd                              -65        10  avgt               83.087          ns/op
 * ChampSetJmh.mRemoveThenAdd                              -65      1000  avgt              192.916          ns/op
 * ChampSetJmh.mRemoveThenAdd                              -65    100000  avgt              315.212          ns/op
 * ChampSetJmh.mRemoveThenAdd                              -65  10000000  avgt              837.040          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeAllRetained      -65        10  avgt              134.594          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeAllRetained      -65      1000  avgt            16371.598          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeAllRetained      -65    100000  avgt          4563910.792          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeAllRetained      -65  10000000  avgt       1718482181.833          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65        10  avgt              117.174          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65      1000  avgt            17332.768          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65    100000  avgt          3382914.628          ns/op
 * ChampSetJmh.mRetainAllFromDifferentTypeNoneRetained     -65  10000000  avgt       1622840678.000          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeAllRetained           -65        10  avgt              102.645          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeAllRetained           -65      1000  avgt            14075.248          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeAllRetained           -65    100000  avgt          1496210.664          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeAllRetained           -65  10000000  avgt        246502737.683          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeNoneRetained          -65        10  avgt               77.052          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeNoneRetained          -65      1000  avgt             8153.361          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeNoneRetained          -65    100000  avgt          1379481.006          ns/op
 * ChampSetJmh.mRetainAllFromSameTypeNoneRetained          -65  10000000  avgt        347464112.690          ns/op
 * ChampSetJmh.mTail                                       -65        10  avgt               20.613          ns/op
 * ChampSetJmh.mTail                                       -65      1000  avgt               48.886          ns/op
 * ChampSetJmh.mTail                                       -65    100000  avgt               88.017          ns/op
 * ChampSetJmh.mTail                                       -65  10000000  avgt              112.837          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g",})
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

/*

    @Benchmark
    public Key mHead() {
        return setA.iterator().next();
    }


    @Benchmark
    public ChampSet<Key> mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        return setA.remove(key).add(key);
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
*/
}
