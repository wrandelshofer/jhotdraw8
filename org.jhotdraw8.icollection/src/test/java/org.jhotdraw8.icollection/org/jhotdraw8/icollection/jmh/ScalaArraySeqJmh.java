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
import scala.collection.IterableOnce;
import scala.collection.Iterator;
import scala.collection.immutable.ArraySeq;
import scala.collection.mutable.Builder;
import scala.jdk.javaapi.CollectionConverters;
import scala.reflect.ClassTag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/// # JMH version: 1.37
/// # VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
/// # Mac Mini M4 Pro
/// # org.scala-lang:scala-library:3.9.0-RC4
///
/// Benchmark                            (size)  Mode  Cnt            Score   Error  Units
/// ScalaArraySeqJmh.mAddAllSameType         10  avgt    2            8.394          ns/op
/// ScalaArraySeqJmh.mAddAllSameType       1000  avgt    2           81.236          ns/op
/// ScalaArraySeqJmh.mAddAllSameType    1000000  avgt    2        79974.569          ns/op
/// ScalaArraySeqJmh.mAddFirst               10  avgt    2            8.170          ns/op
/// ScalaArraySeqJmh.mAddFirst             1000  avgt    2          140.124          ns/op
/// ScalaArraySeqJmh.mAddFirst          1000000  avgt    2        82029.442          ns/op
/// ScalaArraySeqJmh.mAddLast                10  avgt    2            6.478          ns/op
/// ScalaArraySeqJmh.mAddLast              1000  avgt    2           82.169          ns/op
/// ScalaArraySeqJmh.mAddLast           1000000  avgt    2        82659.705          ns/op
/// ScalaArraySeqJmh.mAddOneByOne            10  avgt    2           86.548          ns/op
/// ScalaArraySeqJmh.mAddOneByOne          1000  avgt    2        42692.047          ns/op
/// ScalaArraySeqJmh.mAddOneByOne       1000000  avgt    2  44205854437.500          ns/op
/// ScalaArraySeqJmh.mContainsNotFound       10  avgt    2            3.912          ns/op
/// ScalaArraySeqJmh.mContainsNotFound     1000  avgt    2          236.900          ns/op
/// ScalaArraySeqJmh.mContainsNotFound  1000000  avgt    2      1950150.967          ns/op
/// ScalaArraySeqJmh.mGet                    10  avgt    2            1.860          ns/op
/// ScalaArraySeqJmh.mGet                  1000  avgt    2            2.221          ns/op
/// ScalaArraySeqJmh.mGet               1000000  avgt    2           16.043          ns/op
/// ScalaArraySeqJmh.mHead                   10  avgt    2            0.781          ns/op
/// ScalaArraySeqJmh.mHead                 1000  avgt    2            0.781          ns/op
/// ScalaArraySeqJmh.mHead              1000000  avgt    2            0.849          ns/op
/// ScalaArraySeqJmh.mIterate                10  avgt    2            2.555          ns/op
/// ScalaArraySeqJmh.mIterate              1000  avgt    2          244.199          ns/op
/// ScalaArraySeqJmh.mIterate           1000000  avgt    2      3254426.956          ns/op
/// ScalaArraySeqJmh.mRemoveAtIndex          10  avgt    2           16.843          ns/op
/// ScalaArraySeqJmh.mRemoveAtIndex        1000  avgt    2          176.309          ns/op
/// ScalaArraySeqJmh.mRemoveAtIndex     1000000  avgt    2       170086.337          ns/op
/// ScalaArraySeqJmh.mRemoveLast             10  avgt    2            3.237          ns/op
/// ScalaArraySeqJmh.mRemoveLast           1000  avgt    2           80.014          ns/op
/// ScalaArraySeqJmh.mRemoveLast        1000000  avgt    2        77517.355          ns/op
/// ScalaArraySeqJmh.mReversedIterate        10  avgt    2            3.578          ns/op
/// ScalaArraySeqJmh.mReversedIterate      1000  avgt    2          445.089          ns/op
/// ScalaArraySeqJmh.mReversedIterate   1000000  avgt    2      5355051.789          ns/op
/// ScalaArraySeqJmh.mSet                    10  avgt    2           11.494          ns/op
/// ScalaArraySeqJmh.mSet                  1000  avgt    2          123.301          ns/op
/// ScalaArraySeqJmh.mSet               1000000  avgt    2        70385.160          ns/op
/// ScalaArraySeqJmh.mTail                   10  avgt    2            3.175          ns/op
/// ScalaArraySeqJmh.mTail                 1000  avgt    2          107.556          ns/op
/// ScalaArraySeqJmh.mTail              1000000  avgt    2        83634.125          ns/op
@State(Scope.Benchmark)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unchecked")
public class ScalaArraySeqJmh {
    @Param({"10", "1000", "1000000"})
    private int size;

    private int mask = -65;

    private BenchmarkData data;
    private ArraySeq<Key> listA;
    private ArraySeq<Key> listB;


    private Method updated;
    private int index;


    @Setup
    public void setup() {
        data = new BenchmarkData(size, mask);

        Builder<Key, ArraySeq<Key>> bA = ArraySeq.newBuilder(ClassTag.apply(Key.class));
        for (Key key : data.setA) {
            bA.addOne(key);
        }
        listA = bA.result();


        Builder<Key, ArraySeq<Key>> bB = ArraySeq.newBuilder(ClassTag.apply(Key.class));
        for (Key key : data.setB) {
            bB.addOne(key);
        }
        listB = bB.result();

        data.nextKeyInA();
        try {
            updated = ArraySeq.class.getDeclaredMethod("updated", Integer.TYPE, Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        index = Math.min(listA.length() - 1, BigInteger.valueOf(listA.length() / 2).nextProbablePrime().intValue());
    }

    private Method appendedMethod;
    private Method appendedAllMethod;

    {
        try {
            appendedMethod = ArraySeq.class.getDeclaredMethod("appended", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    {
        try {
            appendedAllMethod = ArraySeq.class.getDeclaredMethod("appendedAll", IterableOnce.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    //    @Benchmark
    public ArraySeq<Key> mAddAll() {
        Builder<Key, ArraySeq<Key>> builder = ArraySeq.newBuilder(ClassTag.apply(Key.class));
        builder.addAll(CollectionConverters.asScala(data.setA));
        return builder.result();
    }

    @Benchmark
    public ArraySeq<Key> mAddAllSameType() throws InvocationTargetException, IllegalAccessException {
        return (ArraySeq<Key>) appendedMethod.invoke(listA, listB);
    }


    @Benchmark
    public ArraySeq<Key> mAddOneByOne() throws InvocationTargetException, IllegalAccessException {
        Builder<Key, ArraySeq<Key>> builder = ArraySeq.newBuilder(ClassTag.apply(Key.class));
        ArraySeq<Key> l = builder.result();
        for (Key key : data.listA) {
            l = (ArraySeq<Key>) appendedMethod.invoke(l, key);
        }
        return l;
    }

    //   @Benchmark
    //   public ArraySeq<Key> mRemoveOneByOne() {
    //       var l = listA;
    //       for (var e : data.listA) {
    //           l = l.removed(e);
    //       }
    //       if (!l.isEmpty()) throw new AssertionError("map: " + l);
    //       return l;
    //   }
//
    //   @Benchmark
    //   public ArraySeq<Key> mRemoveAll() {
    //       ArraySeq<Key> set = listA;
    //       return set.filter(data.listA);
    //   }
//
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
    public ArraySeq<Key> mTail() {
        return listA.tail();
    }

    @Benchmark
    public ArraySeq<Key> mAddLast() {
        Key key = data.nextKeyInB();
        return (ArraySeq<Key>) (listA).$colon$plus(key);
    }

    @Benchmark
    public ArraySeq<Key> mAddFirst() {
        Key key = data.nextKeyInB();
        return (ArraySeq<Key>) (listA).$plus$colon(key);
    }

    @Benchmark
    public ArraySeq<Key> mRemoveLast() {
        return listA.dropRight(1);
    }

    @Benchmark
    public ArraySeq<Key> mRemoveAtIndex() throws InvocationTargetException, IllegalAccessException {
        return (ArraySeq<Key>) appendedAllMethod.invoke(listA.take(index), listA.drop(index));
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
    public ArraySeq<Key> mSet() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int index = data.nextIndexInA();
        Key key = data.nextKeyInB();

        return (ArraySeq<Key>) updated.invoke(listA, index, key);
    }

}
