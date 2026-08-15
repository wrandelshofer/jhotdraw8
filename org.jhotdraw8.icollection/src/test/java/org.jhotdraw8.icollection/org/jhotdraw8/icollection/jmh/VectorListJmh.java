package org.jhotdraw8.icollection.jmh;

import org.jhotdraw8.icollection.PersistentVectorList;
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

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
///
/// Benchmark                         (size)  Mode  Cnt         Score   Error  Units
/// VectorListJmh.mAddAll                 10  avgt    2        19.688          ns/op
/// VectorListJmh.mAddAll               1000  avgt    2      1868.943          ns/op
/// VectorListJmh.mAddAll            1000000  avgt    2   7229218.112          ns/op
/// VectorListJmh.mAddAllArray            10  avgt    2         2.752          ns/op
/// VectorListJmh.mAddAllArray          1000  avgt    2       172.668          ns/op
/// VectorListJmh.mAddAllArray       1000000  avgt    2    177198.346          ns/op
/// VectorListJmh.mAddAllSameType         10  avgt    2        23.574          ns/op
/// VectorListJmh.mAddAllSameType       1000  avgt    2      2721.626          ns/op
/// VectorListJmh.mAddAllSameType    1000000  avgt    2   3210672.504          ns/op
/// VectorListJmh.mAddAt0                 10  avgt    2        15.957          ns/op
/// VectorListJmh.mAddAt0               1000  avgt    2        16.917          ns/op
/// VectorListJmh.mAddAt0            1000000  avgt    2        63.090          ns/op
/// VectorListJmh.mAddFirst               10  avgt    2        15.759          ns/op
/// VectorListJmh.mAddFirst             1000  avgt    2        17.048          ns/op
/// VectorListJmh.mAddFirst          1000000  avgt    2        40.046          ns/op
/// VectorListJmh.mAddLast                10  avgt    2         8.386          ns/op
/// VectorListJmh.mAddLast              1000  avgt    2        14.149          ns/op
/// VectorListJmh.mAddLast           1000000  avgt    2        40.153          ns/op
/// VectorListJmh.mAddOneByOne            10  avgt    2       112.025          ns/op
/// VectorListJmh.mAddOneByOne          1000  avgt    2     15797.886          ns/op
/// VectorListJmh.mAddOneByOne       1000000  avgt    2  50197440.257          ns/op
/// VectorListJmh.mContainsNotFound       10  avgt    2        14.545          ns/op
/// VectorListJmh.mContainsNotFound     1000  avgt    2      1220.544          ns/op
/// VectorListJmh.mContainsNotFound  1000000  avgt    2   1373999.225          ns/op
/// VectorListJmh.mGet                    10  avgt    2         2.085          ns/op
/// VectorListJmh.mGet                  1000  avgt    2         2.620          ns/op
/// VectorListJmh.mGet               1000000  avgt    2        29.706          ns/op
/// VectorListJmh.mHead                   10  avgt    2         1.290          ns/op
/// VectorListJmh.mHead                 1000  avgt    2         1.562          ns/op
/// VectorListJmh.mHead              1000000  avgt    2         2.306          ns/op
/// VectorListJmh.mIterate                10  avgt    2         5.306          ns/op
/// VectorListJmh.mIterate              1000  avgt    2      1383.979          ns/op
/// VectorListJmh.mIterate           1000000  avgt    2   2292078.397          ns/op
/// VectorListJmh.mListIterate            10  avgt    2        10.770          ns/op
/// VectorListJmh.mListIterate          1000  avgt    2      1425.512          ns/op
/// VectorListJmh.mListIterate       1000000  avgt    2   4348326.834          ns/op
/// VectorListJmh.mRemoveAll              10  avgt    2        36.665          ns/op
/// VectorListJmh.mRemoveAll            1000  avgt    2      3439.487          ns/op
/// VectorListJmh.mRemoveAll         1000000  avgt    2   7409836.508          ns/op
/// VectorListJmh.mRemoveAtIndex          10  avgt    2        16.490          ns/op
/// VectorListJmh.mRemoveAtIndex        1000  avgt    2      1823.176          ns/op
/// VectorListJmh.mRemoveAtIndex     1000000  avgt    2   1612455.922          ns/op
/// VectorListJmh.mRemoveLast             10  avgt    2         2.539          ns/op
/// VectorListJmh.mRemoveLast           1000  avgt    2         3.022          ns/op
/// VectorListJmh.mRemoveLast        1000000  avgt    2         3.041          ns/op
/// VectorListJmh.mReversedIterate        10  avgt    2         6.620          ns/op
/// VectorListJmh.mReversedIterate      1000  avgt    2       758.719          ns/op
/// VectorListJmh.mReversedIterate   1000000  avgt    2   2805240.591          ns/op
/// VectorListJmh.mSet                    10  avgt    2         9.558          ns/op
/// VectorListJmh.mSet                  1000  avgt    2        20.376          ns/op
/// VectorListJmh.mSet               1000000  avgt    2       142.574          ns/op
/// VectorListJmh.mTail                   10  avgt    2         2.512          ns/op
/// VectorListJmh.mTail                 1000  avgt    2         3.079          ns/op
/// VectorListJmh.mTail              1000000  avgt    2         3.100          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorListJmh {
    @Param({"10", "1000", "100000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private PersistentVectorList<Key> listA;
    private PersistentVectorList<Key> listB;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = PersistentVectorList.of();
        for (Key key : data.setA) {
            listA = listA.adding(key);
        }
        listB = PersistentVectorList.copyOf(data.setB);
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    @Benchmark
    public PersistentVectorList<Key> mCopyOf() {
        return PersistentVectorList.copyOf(data.setA);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddAll() {
        return listA.addingAll(data.listB);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddAllSameType() {
        return listA.addingAll(listB);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddAllArray() {
        return PersistentVectorList.<Key>of(data.arrayA);
    }

    @Benchmark
    public PersistentVectorList<Key> mAddOneByOne() {
        PersistentVectorList<Key> l = PersistentVectorList.of();
        for (Key key : data.listA) {
            l = l.adding(key);
        }
        return l;
    }


    /// This appears to be broken!
    @Benchmark
    public PersistentVectorList<Key> mRemoveOneByOne() {
        var l = listA;
        for (var e : data.listA) {
            l = l.removing(e);
        }
        if (!l.isEmpty()) throw new AssertionError("map: " + l);
        return l;
    }

    /*
    @Benchmark
    public VectorList<Key> mRemoveAll() {
        VectorList<Key> l = listA;
        return l.removeAll(data.setA);
    }


        @Benchmark
        public int mIterate() {
            int sum = 0;
            for (Iterator<Key> i = listA.iterator(); i.hasNext(); ) {
                sum += i.next().value;
            }
            return sum;
        }

        @Benchmark
        public int mListIterate() {
            int sum = 0;
            for (Iterator<Key> i = listA.listIterator(); i.hasNext(); ) {
                sum += i.next().value;
            }
            return sum;
        }

        @Benchmark
        public int mReversedIterate() {
            int sum = 0;
            for (int i = listA.size() - 1; i >= 0; i--) {
                sum += listA.get(i).value;
            }
            return sum;
        }

        @Benchmark
        public VectorList<Key> mTail() {
            return listA.removeAt(0);
        }

        @Benchmark
        public VectorList<Key> mAddLast() {
            Key key = data.nextKeyInB();
            return (listA).add(key);
        }

        @Benchmark
        public VectorList<Key> mAddFirst() {
            Key key = data.nextKeyInB();
            return (listA).addFirst(key);
        }

        @Benchmark
        public VectorList<Key> mAddAt0() {
            Key key = data.nextKeyInB();
            return (listA).add(0, key);
        }


        @Benchmark
        public VectorList<Key> mRemoveLast() {
            return listA.removeAt(listA.size() - 1);
        }
    */
    @Benchmark
    public PersistentVectorList<Key> mRemoveAtIndex() {
        return listA.removingAt(index);
    }
/*
    @Benchmark
    public Key mGet() {
        int offset = data.nextIndexInA();
        return listA.get(offset);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return listA.get(0);
    }

    @Benchmark
    public VectorList<Key> mSet() {
        int offset = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(offset, key);
    }*/
}
