package org.jhotdraw8.immutable_collection.jmh;

import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark           (size)  Mode  Cnt         Score   Error  Units
 * mAddAll                 10  avgt             38.780          ns/op
 * mAddAll               1000  avgt           3517.430          ns/op
 * mAddAll            1000000  avgt       10883088.254          ns/op
 * mAddFirst               10  avgt             39.391          ns/op
 * mAddFirst             1000  avgt            796.830          ns/op
 * mAddFirst          1000000  avgt        1022880.566          ns/op
 * mAddLast                10  avgt             22.441          ns/op
 * mAddLast              1000  avgt            750.063          ns/op
 * mAddLast           1000000  avgt         954195.059          ns/op
 * mAddOneByOne            10  avgt             63.707          ns/op
 * mAddOneByOne          1000  avgt           7157.487          ns/op
 * mAddOneByOne       1000000  avgt       28348039.603          ns/op
 * mContainsNotFound       10  avgt              8.953          ns/op
 * mContainsNotFound     1000  avgt            521.739          ns/op
 * mContainsNotFound  1000000  avgt        3440927.995          ns/op
 * mGet                    10  avgt              3.835          ns/op
 * mGet                  1000  avgt              4.242          ns/op
 * mGet               1000000  avgt             56.187          ns/op
 * mHead                   10  avgt              1.130          ns/op
 * mHead                 1000  avgt              1.100          ns/op
 * mHead              1000000  avgt              1.098          ns/op
 * mIterate                10  avgt              9.934          ns/op
 * mIterate              1000  avgt            769.100          ns/op
 * mIterate           1000000  avgt        4788183.321          ns/op
 * mListIterate            10  avgt              9.952          ns/op
 * mListIterate          1000  avgt            784.239          ns/op
 * mListIterate       1000000  avgt        4621496.623          ns/op
 * mSpliterate             10  avgt             12.750          ns/op
 * mSpliterate           1000  avgt           1250.040          ns/op
 * mSpliterate        1000000  avgt        7351715.413          ns/op
 * mRemoveAtIndex          10  avgt             21.961          ns/op
 * mRemoveAtIndex        1000  avgt            311.395          ns/op
 * mRemoveAtIndex     1000000  avgt         398696.213          ns/op
 * mRemoveLast             10  avgt              9.985          ns/op
 * mRemoveLast           1000  avgt            308.132          ns/op
 * mRemoveLast        1000000  avgt         355067.797          ns/op
 * mReversedIterate        10  avgt              7.674          ns/op
 * mReversedIterate      1000  avgt            779.323          ns/op
 * mReversedIterate   1000000  avgt        4510635.504          ns/op
 * mSet                    10  avgt              6.197          ns/op
 * mSet                  1000  avgt              7.288          ns/op
 * mSet               1000000  avgt            150.787          ns/op
 * mTail                   10  avgt             20.963          ns/op
 * mTail                 1000  avgt            328.766          ns/op
 * mTail              1000000  avgt         495768.352          ns/op
 *
 * Process finished with exit code 0
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xmx28g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ArrayListJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private ArrayList<Key> listA;

    private int index;

    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        listA = new ArrayList();
        for (Key key : data.setA) {
            listA.add(key);
        }
        index = Math.min(listA.size() - 1, BigInteger.valueOf(listA.size() / 2).nextProbablePrime().intValue());
    }


    //@Benchmark
    public ArrayList<Key> mAddAll() {
        return new ArrayList<>(data.setA);
    }

    //@Benchmark
    public ArrayList<Key> mAddOneByOne() {
        ArrayList<Key> set = new ArrayList<>();
        for (Key key : data.listA) {
            set.add(key);
        }
        return set;
    }

    ////@Benchmark
    public ArrayList<Key> mRemoveOneByOne() {
        var map = (ArrayList<Key>) listA.clone();
        for (var e : data.listA) {
            map.remove(e);
        }
        if (!map.isEmpty()) throw new AssertionError("map: " + map);
        return map;
    }

    ////@Benchmark
    public boolean mRemoveAll() {
        ArrayList<Key> set = ((ArrayList<Key>) listA.clone());
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

    //@Benchmark
    public int mListIterate() {
        int sum = 0;
        for (Iterator<Key> i = listA.listIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    //@Benchmark
    public int mReversedIterate() {
        int sum = 0;
        for (int i = listA.size() - 1; i >= 0; i--) {
            sum += listA.get(i).value;
        }
        return sum;
    }

    @Benchmark
    public int mSpliterate() {
        class Sum implements Consumer<Key> {
            int sum;

            @Override
            public void accept(Key key) {
                sum += key.value;
            }
        }
        Sum sum = new Sum();
        Spliterator<Key> i = listA.spliterator();
        while (i.tryAdvance(sum)) {
        }
        return sum.sum;
    }

    //@Benchmark
    public Key mTail() {
        return ((ArrayList<Key>) listA.clone()).remove(0);
    }

    //@Benchmark
    public boolean mAddLast() {
        Key key = data.nextKeyInB();
        return ((ArrayList<Key>) listA.clone()).add(key);
    }

    //@Benchmark
    public int mAddFirst() {
        Key key = data.nextKeyInB();
        ArrayList<Key> clone = ((ArrayList<Key>) listA.clone());
        clone.add(0, key);
        return clone.size();
    }


    //@Benchmark
    public Key mRemoveLast() {
        ArrayList<Key> clone = ((ArrayList<Key>) listA.clone());
        return clone.remove(clone.size() - 1);
    }

    //@Benchmark
    public Key mRemoveAtIndex() {
        ArrayList<Key> clone = ((ArrayList<Key>) listA.clone());
        return clone.remove(index);
    }

    //@Benchmark
    public Key mGet() {
        int index = data.nextIndexInA();
        return listA.get(index);
    }

    //@Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    //@Benchmark
    public Key mHead() {
        return listA.get(0);
    }

    //@Benchmark
    public Key mSet() {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();
        return listA.set(index, key);
    }


}
