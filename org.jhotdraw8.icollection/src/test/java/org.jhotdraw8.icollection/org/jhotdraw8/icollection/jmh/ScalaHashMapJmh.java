package org.jhotdraw8.icollection.jmh;

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

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro, 4.40 GHz
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                          (mask)   (size)  Mode  Cnt          Score   Error  Units
/// ScalaHashMapJmh.mAddOneByOne          -65       10  avgt    2        204.563          ns/op
/// ScalaHashMapJmh.mAddOneByOne          -65     1000  avgt    2      39373.131          ns/op
/// ScalaHashMapJmh.mAddOneByOne          -65  1000000  avgt    2  316267579.438          ns/op
/// ScalaHashMapJmh.mContainsFound        -65       10  avgt    2          2.406          ns/op
/// ScalaHashMapJmh.mContainsFound        -65     1000  avgt    2          6.201          ns/op
/// ScalaHashMapJmh.mContainsFound        -65  1000000  avgt    2         84.503          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65       10  avgt    2          2.628          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65     1000  avgt    2          6.427          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65  1000000  avgt    2         75.253          ns/op
/// ScalaHashMapJmh.mCopyOf               -65       10  avgt    2        163.336          ns/op
/// ScalaHashMapJmh.mCopyOf               -65     1000  avgt    2      33850.684          ns/op
/// ScalaHashMapJmh.mCopyOf               -65  1000000  avgt    2  184351799.627          ns/op
/// ScalaHashMapJmh.mHead                 -65       10  avgt    2          1.113          ns/op
/// ScalaHashMapJmh.mHead                 -65     1000  avgt    2          1.556          ns/op
/// ScalaHashMapJmh.mHead                 -65  1000000  avgt    2          9.592          ns/op
/// ScalaHashMapJmh.mIterate              -65       10  avgt    2          4.032          ns/op
/// ScalaHashMapJmh.mIterate              -65     1000  avgt    2       1441.451          ns/op
/// ScalaHashMapJmh.mIterate              -65  1000000  avgt    2    8711273.607          ns/op
/// ScalaHashMapJmh.mPut                  -65       10  avgt    2          5.637          ns/op
/// ScalaHashMapJmh.mPut                  -65     1000  avgt    2         28.905          ns/op
/// ScalaHashMapJmh.mPut                  -65  1000000  avgt    2        253.571          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65       10  avgt    2        127.876          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65     1000  avgt    2      39567.810          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65  1000000  avgt    2  347888534.483          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65       10  avgt    2        147.005          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65     1000  avgt    2      39972.128          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65  1000000  avgt    2  338093713.900          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65       10  avgt    2         28.924          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65     1000  avgt    2         80.566          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65  1000000  avgt    2        391.115          ns/op
/// ScalaHashMapJmh.mTail                 -65       10  avgt    2         13.174          ns/op
/// ScalaHashMapJmh.mTail                 -65     1000  avgt    2         22.447          ns/op
/// ScalaHashMapJmh.mTail                 -65  1000000  avgt    2         51.296          ns/op
///
///
/// <pre>
/// # JMH version: 1.36
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
/// jvmArgsAppend = {"-ea", "-Xmx28g","-XX:-Inline"})
/// Benchmark                 (mask)  (size)  Mode  Cnt        Score   Error  Units
/// ScalaHashMapJmh.mIterate     -65  100000  avgt       1800621.175          ns/op
/// </pre>
/// <pre>
/// # JMH version: 1.36
/// # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
/// # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
///
/// Benchmark                          (mask)    (size)  Mode  Cnt           Score   Error  Units
/// ScalaHashMapJmh.mAddOneByOne          -65        10  avgt              403.495          ns/op
/// ScalaHashMapJmh.mAddOneByOne          -65      1000  avgt           123815.275          ns/op
/// ScalaHashMapJmh.mAddOneByOne          -65    100000  avgt         25323173.593          ns/op
/// ScalaHashMapJmh.mAddOneByOne          -65  10000000  avgt       6787855402.000          ns/op
/// ScalaHashMapJmh.mContainsFound        -65        10  avgt                7.642          ns/op
/// ScalaHashMapJmh.mContainsFound        -65      1000  avgt               16.070          ns/op
/// ScalaHashMapJmh.mContainsFound        -65    100000  avgt               53.218          ns/op
/// ScalaHashMapJmh.mContainsFound        -65  10000000  avgt              364.477          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65        10  avgt                7.649          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65      1000  avgt               16.182          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65    100000  avgt               52.740          ns/op
/// ScalaHashMapJmh.mContainsNotFound     -65  10000000  avgt              345.889          ns/op
/// ScalaHashMapJmh.mCopyOf               -65        10  avgt              425.306          ns/op
/// ScalaHashMapJmh.mCopyOf               -65      1000  avgt           105113.208          ns/op
/// ScalaHashMapJmh.mCopyOf               -65    100000  avgt         20062013.475          ns/op
/// ScalaHashMapJmh.mCopyOf               -65  10000000  avgt       5799521206.000          ns/op
/// ScalaHashMapJmh.mHead                 -65        10  avgt                1.664          ns/op
/// ScalaHashMapJmh.mHead                 -65      1000  avgt               11.732          ns/op
/// ScalaHashMapJmh.mHead                 -65    100000  avgt               19.463          ns/op
/// ScalaHashMapJmh.mHead                 -65  10000000  avgt               32.496          ns/op
/// ScalaHashMapJmh.mIterateEntry         -65        10  avgt    2           4.113          ns/op
/// ScalaHashMapJmh.mIterateEntry         -65      1000  avgt    2        1449.925          ns/op
/// ScalaHashMapJmh.mIterateEntry         -65   1000000  avgt    2     9828021.104          ns/op
/// ScalaHashMapJmh.mIterateKey           -65        10  avgt    2           4.042          ns/op
/// ScalaHashMapJmh.mIterateKey           -65      1000  avgt    2        1489.129          ns/op
/// ScalaHashMapJmh.mIterateKey           -65   1000000  avgt    2    10417747.001          ns/op
/// ScalaHashMapJmh.mIterate              -65  10000000  avgt        372202721.630          ns/op
/// ScalaHashMapJmh.mPut                  -65        10  avgt               14.998          ns/op
/// ScalaHashMapJmh.mPut                  -65      1000  avgt               61.352          ns/op
/// ScalaHashMapJmh.mPut                  -65    100000  avgt              171.648          ns/op
/// ScalaHashMapJmh.mPut                  -65  10000000  avgt              938.612          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65        10  avgt              325.152          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65      1000  avgt           119577.774          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65    100000  avgt         28614259.411          ns/op
/// ScalaHashMapJmh.mRemoveAll            -65  10000000  avgt       7805864605.500          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65        10  avgt              374.348          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65      1000  avgt           122365.736          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65    100000  avgt         28782979.175          ns/op
/// ScalaHashMapJmh.mRemoveOneByOne       -65  10000000  avgt       7029372693.000          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65        10  avgt               90.685          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65      1000  avgt              263.081          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65    100000  avgt              378.232          ns/op
/// ScalaHashMapJmh.mRemoveThenAdd        -65  10000000  avgt             1256.939          ns/op
/// ScalaHashMapJmh.mTail                 -65        10  avgt               36.773          ns/op
/// ScalaHashMapJmh.mTail                 -65      1000  avgt               73.006          ns/op
/// ScalaHashMapJmh.mTail                 -65    100000  avgt               92.645          ns/op
/// ScalaHashMapJmh.mTail                 -65  10000000  avgt              143.238          ns/op
/// </pre>
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaHashMapJmh {
    @Param({"10", "1000", "100000"})
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
        public HashMap<Key, Boolean> mRemoveAll() {
            HashMap<Key, Boolean> set = mapA;
            return set.removedAll(listAKeys);
        }
    */
    @Benchmark
    public int mIterateKey() {
        int sum = 0;
        for (Iterator<Key> i = mapA.keysIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public int mIterateEntry() {
        int sum = 0;
        for (var i = mapA.iterator(); i.hasNext(); ) {
            sum += i.next()._1.value;
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
