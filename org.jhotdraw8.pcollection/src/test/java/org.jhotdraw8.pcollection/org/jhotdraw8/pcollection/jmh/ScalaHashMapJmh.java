package org.jhotdraw8.pcollection.jmh;

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
 * jvmArgsAppend = {"-ea", "-Xmx28g","-XX:-Inline"})
 * Benchmark                 (mask)  (size)  Mode  Cnt        Score   Error  Units
 * ScalaHashMapJmh.mIterate     -65  100000  avgt       1800621.175          ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                          (mask)    (size)  Mode  Cnt           Score   Error  Units
 * ScalaHashMapJmh.mAddOneByOne          -65        10  avgt              403.495          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65      1000  avgt           123815.275          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65    100000  avgt         25323173.593          ns/op
 * ScalaHashMapJmh.mAddOneByOne          -65  10000000  avgt       6787855402.000          ns/op
 * ScalaHashMapJmh.mContainsFound        -65        10  avgt                7.642          ns/op
 * ScalaHashMapJmh.mContainsFound        -65      1000  avgt               16.070          ns/op
 * ScalaHashMapJmh.mContainsFound        -65    100000  avgt               53.218          ns/op
 * ScalaHashMapJmh.mContainsFound        -65  10000000  avgt              364.477          ns/op
 * ScalaHashMapJmh.mContainsNotFound     -65        10  avgt                7.649          ns/op
 * ScalaHashMapJmh.mContainsNotFound     -65      1000  avgt               16.182          ns/op
 * ScalaHashMapJmh.mContainsNotFound     -65    100000  avgt               52.740          ns/op
 * ScalaHashMapJmh.mContainsNotFound     -65  10000000  avgt              345.889          ns/op
 * ScalaHashMapJmh.mCopyOf               -65        10  avgt              425.306          ns/op
 * ScalaHashMapJmh.mCopyOf               -65      1000  avgt           105113.208          ns/op
 * ScalaHashMapJmh.mCopyOf               -65    100000  avgt         20062013.475          ns/op
 * ScalaHashMapJmh.mCopyOf               -65  10000000  avgt       5799521206.000          ns/op
 * ScalaHashMapJmh.mHead                 -65        10  avgt                1.664          ns/op
 * ScalaHashMapJmh.mHead                 -65      1000  avgt               11.732          ns/op
 * ScalaHashMapJmh.mHead                 -65    100000  avgt               19.463          ns/op
 * ScalaHashMapJmh.mHead                 -65  10000000  avgt               32.496          ns/op
 * ScalaHashMapJmh.mIterate              -65        10  avgt                9.484          ns/op
 * ScalaHashMapJmh.mIterate              -65      1000  avgt             3135.325          ns/op
 * ScalaHashMapJmh.mIterate              -65    100000  avgt           934525.883          ns/op
 * ScalaHashMapJmh.mIterate              -65  10000000  avgt        372202721.630          ns/op
 * ScalaHashMapJmh.mPut                  -65        10  avgt               14.998          ns/op
 * ScalaHashMapJmh.mPut                  -65      1000  avgt               61.352          ns/op
 * ScalaHashMapJmh.mPut                  -65    100000  avgt              171.648          ns/op
 * ScalaHashMapJmh.mPut                  -65  10000000  avgt              938.612          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65        10  avgt              325.152          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65      1000  avgt           119577.774          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65    100000  avgt         28614259.411          ns/op
 * ScalaHashMapJmh.mRemoveAll            -65  10000000  avgt       7805864605.500          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65        10  avgt              374.348          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65      1000  avgt           122365.736          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65    100000  avgt         28782979.175          ns/op
 * ScalaHashMapJmh.mRemoveOneByOne       -65  10000000  avgt       7029372693.000          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        -65        10  avgt               90.685          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        -65      1000  avgt              263.081          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        -65    100000  avgt              378.232          ns/op
 * ScalaHashMapJmh.mRemoveThenAdd        -65  10000000  avgt             1256.939          ns/op
 * ScalaHashMapJmh.mTail                 -65        10  avgt               36.773          ns/op
 * ScalaHashMapJmh.mTail                 -65      1000  avgt               73.006          ns/op
 * ScalaHashMapJmh.mTail                 -65    100000  avgt               92.645          ns/op
 * ScalaHashMapJmh.mTail                 -65  10000000  avgt              143.238          ns/op
 * </pre>
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-ea", "-Xmx28g", "-XX:-Inline"})
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
    public HashMap<Key, Boolean> mRemoveAll() {
        HashMap<Key, Boolean> set = mapA;
        return set.removedAll(listAKeys);
    }

    @Benchmark
    public int mIterate() {
        int sum = 0;
        for (Iterator<Key> i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

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

}
