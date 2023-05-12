package org.jhotdraw8.collection.jmh;

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
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.HashMap;
import scala.collection.immutable.Vector;
import scala.collection.mutable.Builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                           (size)  Mode  Cnt     _     Score   Error  Units
 * ScalaHashMapJmh.mCopyOf               -65        10  avgt               467.142          ns/op
 * ScalaHashMapJmh.mCopyOf               -65      1000  avgt            114499.940          ns/op
 * ScalaHashMapJmh.mCopyOf               -65    100000  avgt          23510614.310          ns/op
 * ScalaHashMapJmh.mCopyOf               -65  10000000  avgt        7447239207.500          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65        10  avgt               432.536          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65      1000  avgt            138463.447          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65    100000  avgt          35389172.339          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65  10000000  avgt       10663694719.000          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65        10  avgt               384.790          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65      1000  avgt            126641.616          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65    100000  avgt          32877551.174          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65  10000000  avgt       14457074260.000          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65        10  avgt               373.129          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65      1000  avgt            134244.683          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65    100000  avgt          34034988.668          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65  10000000  avgt       12629623452.000          ns/op
 * ScalaHashMapJmh.mContainsFound          10  avgt    2     _     6.293          ns/op
 * ScalaHashMapJmh.mContainsFound         100  avgt    2     _    11.190          ns/op
 * ScalaHashMapJmh.mContainsFound        1000  avgt    2     _    19.242          ns/op
 * ScalaHashMapJmh.mContainsFound       10000  avgt    2     _    37.319          ns/op
 * ScalaHashMapJmh.mContainsFound      100000  avgt    2     _    61.474          ns/op
 * ScalaHashMapJmh.mContainsFound     1000000  avgt    2     _   236.853          ns/op
 * ScalaHashMapJmh.mContainsNotFound       10  avgt    2     _     6.142          ns/op
 * ScalaHashMapJmh.mContainsNotFound      100  avgt    2     _    11.060          ns/op
 * ScalaHashMapJmh.mContainsNotFound     1000  avgt    2     _    18.948          ns/op
 * ScalaHashMapJmh.mContainsNotFound    10000  avgt    2     _    36.833          ns/op
 * ScalaHashMapJmh.mContainsNotFound   100000  avgt    2     _    62.170          ns/op
 * ScalaHashMapJmh.mContainsNotFound  1000000  avgt    2     _   236.771          ns/op
 * ScalaHashMapJmh.mHead                   10  avgt    2     _     1.703          ns/op
 * ScalaHashMapJmh.mHead                  100  avgt    2     _     8.889          ns/op
 * ScalaHashMapJmh.mHead                 1000  avgt    2     _     9.973          ns/op
 * ScalaHashMapJmh.mHead                10000  avgt    2     _    17.188          ns/op
 * ScalaHashMapJmh.mHead               100000  avgt    2     _    17.328          ns/op
 * ScalaHashMapJmh.mHead              1000000  avgt    2     _    23.052          ns/op
 * ScalaHashMapJmh.mIterate                10  avgt    2     _     9.738          ns/op
 * ScalaHashMapJmh.mIterate               100  avgt    2     _   333.533          ns/op
 * ScalaHashMapJmh.mIterate              1000  avgt    2     _  3001.226          ns/op
 * ScalaHashMapJmh.mIterate             10000  avgt    2     _ 41247.895          ns/op
 * ScalaHashMapJmh.mIterate       -65  100000  avgt    4  986192.276 ± 111403.283  ns/op
 * ScalaHashMapJmh.mIterate           1000000  avgt    2   41_417461.544          ns/op
 * ScalaHashMapJmh.mPut                    10  avgt    2     _    19.341          ns/op
 * ScalaHashMapJmh.mPut                   100  avgt    2     _    38.989          ns/op
 * ScalaHashMapJmh.mPut                  1000  avgt    2     _    60.961          ns/op
 * ScalaHashMapJmh.mPut                 10000  avgt    2     _    92.897          ns/op
 * ScalaHashMapJmh.mPut                100000  avgt    2     _   159.113          ns/op
 * ScalaHashMapJmh.mPut               1000000  avgt    2     _   391.962          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd          10  avgt    2     _    87.681          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd         100  avgt    2     _   128.919          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        1000  avgt    2     _   265.190          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd       10000  avgt    2     _   269.952          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd      100000  avgt    2     _   350.621          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd     1000000  avgt    2     _   670.761          ns/op
 * ScalaHashMapJmh.mTail                   10  avgt    2     _    36.535          ns/op
 * ScalaHashMapJmh.mTail                  100  avgt    2     _    44.574          ns/op
 * ScalaHashMapJmh.mTail                 1000  avgt    2     _    72.841          ns/op
 * ScalaHashMapJmh.mTail                10000  avgt    2     _    92.309          ns/op
 * ScalaHashMapJmh.mTail               100000  avgt    2     _    90.661          ns/op
 * ScalaHashMapJmh.mTail              1000000  avgt    2     _   113.395          ns/op
 *
 * Process finished with exit code 0
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 4)
@Warmup(iterations = 4)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ScalaHashMapJmh {
    @Param({/*"10", "1000",*/ "100000"/*, "10000000"*/})
    private int size;

    @Param({"-65"})
    private int mask;

    private BenchmarkData data;
    private HashMap<Key, Boolean> mapA;
    private Vector<Tuple2<Key, Boolean>> listA;
    private Vector<Key> listAKeys;
    private Method appended;


    @SuppressWarnings("unchecked")
    @Setup
    public void setup() throws InvocationTargetException, IllegalAccessException {
        try {
            appended = Vector.class.getDeclaredMethod("appended", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        data = new BenchmarkData(size, mask);
        Builder<Tuple2<Key, Boolean>, HashMap<Key, Boolean>> b = HashMap.newBuilder();
        for (Key key : data.setA) {
            Tuple2<Key, Boolean> elem = new Tuple2<>(key, Boolean.TRUE);
            b.addOne(elem);
        }
        listA = Vector.<Tuple2<Key, Boolean>>newBuilder().result();
        listAKeys = Vector.<Key>newBuilder().result();
        for (Key key : data.listA) {
            Tuple2<Key, Boolean> elem = new Tuple2<>(key, Boolean.TRUE);
            listA = (Vector<Tuple2<Key, Boolean>>) appended.invoke(listA, elem);
            listAKeys = (Vector<Key>) appended.invoke(listAKeys, key);
        }
        mapA = b.result();

    }

    /*
        @Benchmark
        public HashMap<Key, Boolean> mCopyOf() {
            return HashMap.from(listA);
        }

        @Benchmark
        public HashMap<Key, Boolean> mAddOneByOne() {
            HashMap<Key, Boolean> set = HashMap.<Key, Boolean>newBuilder().result();
            for (Key key : data.listA) {
                set = set.updated(key, Boolean.TRUE);
            }
            return set;
        }

        @Benchmark
        public HashMap<Key, Boolean> mRemoveOneByOne() {
            HashMap<Key, Boolean> set = mapA;
            for (Key key : data.listA) {
                set = set.removed(key);
            }
            return set;
        }

        @Benchmark
        public Map<Key, Boolean> mRemoveAll() {
            HashMap<Key, Boolean> set = mapA;
            return set.removedAll(listAKeys);
        }
    */
    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Iterator<Key> i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }
/*
    @Benchmark
    public void mRemoveThenAdd() {
        Key key = data.nextKeyInA();
        mapA.$minus(key).$plus(new Tuple2<>(key, Boolean.TRUE));
    }

    @Benchmark
    public void mPut() {
        Key key = data.nextKeyInA();
        mapA.$plus(new Tuple2<>(key, Boolean.FALSE));
    }

    @Benchmark
    public boolean mContainsFound() {
        Key key = data.nextKeyInA();
        return mapA.contains(key);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return mapA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return mapA.head()._1;
    }

    @Benchmark
    public HashMap<Key, Boolean> mTail() {
        return mapA.tail();
    }
*/
}
