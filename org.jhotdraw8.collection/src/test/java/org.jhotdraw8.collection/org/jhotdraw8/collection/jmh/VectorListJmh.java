package org.jhotdraw8.collection.jmh;

import org.jhotdraw8.collection.VectorList;
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

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark           (size)  Mode  Cnt          Score   Error  Units
 * mAddAll                 10  avgt              52.805          ns/op
 * mAddAll               1000  avgt            3875.245          ns/op
 * mAddAll            1000000  avgt        11166047.498          ns/op
 * mAddAllArray            10  avgt              36.627          ns/op
 * mAddAllArray          1000  avgt            3831.702          ns/op
 * mAddAllArray       1000000  avgt        18233901.719          ns/op
 * mAddFirst               10  avgt              43.874          ns/op
 * mAddFirst             1000  avgt              50.507          ns/op
 * mAddFirst          1000000  avgt             140.191          ns/op
 * mAddLast                10  avgt              22.597          ns/op
 * mAddLast              1000  avgt              34.994          ns/op
 * mAddLast           1000000  avgt             127.617          ns/op
 * mAddOneByOne            10  avgt             211.522          ns/op
 * mAddOneByOne          1000  avgt           43554.723          ns/op
 * mAddOneByOne       1000000  avgt       134181586.813          ns/op
 * mContainsNotFound       10  avgt              27.532          ns/op
 * mContainsNotFound     1000  avgt            2794.591          ns/op
 * mContainsNotFound  1000000  avgt         5675693.984          ns/op
 * mGet                    10  avgt               3.691          ns/op
 * mGet                  1000  avgt               7.463          ns/op
 * mGet               1000000  avgt              86.203          ns/op
 * mHead                   10  avgt               1.747          ns/op
 * mHead                 1000  avgt               2.236          ns/op
 * mHead              1000000  avgt               5.083          ns/op
 * mIterate                10  avgt              12.636          ns/op
 * mIterate              1000  avgt            1426.097          ns/op
 * mIterate           1000000  avgt        12576434.361          ns/op
 * mRemoveAtIndex          10  avgt              40.534          ns/op
 * mRemoveAtIndex        1000  avgt            3046.037          ns/op
 * mRemoveAtIndex     1000000  avgt         3367569.843          ns/op
 * mRemoveLast             10  avgt              11.158          ns/op
 * mRemoveLast           1000  avgt              12.521          ns/op
 * mRemoveLast        1000000  avgt              10.877          ns/op
 * mReversedIterate        10  avgt              10.123          ns/op
 * mReversedIterate      1000  avgt            1841.430          ns/op
 * mReversedIterate   1000000  avgt        12294983.693          ns/op
 * mSet                    10  avgt              19.737          ns/op
 * mSet                  1000  avgt              40.689          ns/op
 * mSet               1000000  avgt             221.233          ns/op
 * mTail                   10  avgt               6.223          ns/op
 * mTail                 1000  avgt               6.307          ns/op
 * mTail              1000000  avgt               6.358          ns/op
 *
 * Process finished with exit code 0
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class VectorListJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private VectorList<Key> listA;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = VectorList.of();
        for (Key key : data.setA) {
            listA = listA.add(key);
        }
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }

    /*
        @Benchmark
        public VectorList<Key> mAddAll() {
            return VectorList.copyOf(data.setA);
        }        @Benchmark
        public VectorList<Key> mAddAllArray() {
            return VectorList.<Key>of(data.setA.toArray(new Key[0]));
        }

        @Benchmark
        public VectorList<Key> mAddOneByOne() {
            VectorList<Key> set = VectorList.of();
            for (Key key : data.listA) {
                set = set.add(key);
            }
            return set;
        }

        //@Benchmark
        public VectorList<Key> mRemoveOneByOne() {
            var map = listA;
            for (var e : data.listA) {
                map = map.remove(e);
            }
            if (!map.isEmpty()) throw new AssertionError("map: " + map);
            return map;
        }

        //@Benchmark
        public VectorList<Key> mRemoveAll() {
            VectorList<Key> set = listA;
            return set.removeAll(data.listA);
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
        public int mReversedIterate() {
            int sum = 0;
            for (int i=listA.size()-1;i>=0;i--) {
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
            return (listA).add(0,key);
        }
*/


    @Benchmark
    public VectorList<Key> mRemoveLast() {
        return listA.removeAt(listA.size() - 1);
    }

    @Benchmark
    public VectorList<Key> mRemoveAtIndex() {
        return listA.removeAt(index);
    }
/*
    @Benchmark
    public Key mGet() {
        int index = data.nextIndexInA();
        return listA.get(index);
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
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(index, key);
    }

 */
}
