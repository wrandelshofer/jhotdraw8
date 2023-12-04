package org.jhotdraw8.immutable_collection.jmh;

import org.openjdk.jmh.annotations.*;
import scala.collection.Iterator;
import scala.collection.immutable.Vector;
import scala.collection.mutable.ReusableBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.36
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * # org.scala-lang:scala-library:2.13.8
 *
 * Benchmark           (size)  Mode  Cnt         Score   Error  Units
 * mAddFirst               10  avgt             27.796          ns/op
 * mAddFirst          1000000  avgt            320.989          ns/op
 * mAddLast                10  avgt             24.118          ns/op
 * mAddLast           1000000  avgt            207.482          ns/op
 * mContainsNotFound       10  avgt             14.826          ns/op
 * mContainsNotFound  1000000  avgt       20864102.835          ns/op
 * mGet                    10  avgt              4.311          ns/op
 * mGet               1000000  avgt            198.885          ns/op
 * mHead                   10  avgt              1.082          ns/op
 * mHead              1000000  avgt              1.082          ns/op
 * mIterate                10  avgt             11.180          ns/op
 * mIterate           1000000  avgt       32438888.398          ns/op
 * mRemoveAtIndex          10  avgt    2        51.895          ns/op
 * mRemoveAtIndex        1000  avgt    2       287.529          ns/op
 * mRemoveAtIndex     1000000  avgt    2       936.376          ns/op
 * mRemoveLast             10  avgt    2        12.412          ns/op
 * mRemoveLast           1000  avgt    2        41.881          ns/op
 * mRemoveLast        1000000  avgt    2        80.044          ns/op
 * mReversedIterate        10  avgt             10.555          ns/op
 * mReversedIterate   1000000  avgt       43129266.738          ns/op
 * mTail                   10  avgt             18.878          ns/op
 * mTail              1000000  avgt             46.531          ns/op
 * mSet                    10  avgt             33.717          ns/op
 * mSet               1000000  avgt            847.992          ns/op
 */
@State(Scope.Benchmark)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaVectorJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private Vector<Key> listA;


    private Method updated;
    private int index;


    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);
        ReusableBuilder<Key, Vector<Key>> b = Vector.newBuilder();
        for (Key key : data.setA) {
            b.addOne(key);
        }
        listA = b.result();

        data.nextKeyInA();
        try {
            updated = Vector.class.getDeclaredMethod("updated", Integer.TYPE, Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        index = Math.min(listA.length() - 1, BigInteger.valueOf(listA.length() / 2).nextProbablePrime().intValue());
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
        for (Iterator<Key> i = listA.reverseIterator(); i.hasNext(); ) {
            sum += i.next().value;
        }
        return sum;
    }

    @Benchmark
    public Vector<Key> mTail() {
        return listA.tail();
    }

    @Benchmark
    public Vector<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (Vector<Key>) (listA).$colon$plus(key);
    }

    @Benchmark
    public Vector<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (Vector<Key>) (listA).$plus$colon(key);
    }

    @Benchmark
    public Vector<Key> mRemoveLast() {
        return listA.dropRight(1);
    }

    @Benchmark
    public Vector<Key> mRemoveAtIndex() {
        return listA.take(index).appendedAll(listA.drop(index));
    }

    @Benchmark
    public Key mGet() {
        int index = data.nextIndexInA();
        return listA.apply(index);
    }

    @Benchmark
    public boolean mContainsNotFound() {
        Key key = data.nextKeyInB();
        return listA.contains(key);
    }

    @Benchmark
    public Key mHead() {
        return listA.head();
    }

    @Benchmark
    public Vector<Key> mSet() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();

        return (Vector<Key>) updated.invoke(listA, index, key);
    }

}
