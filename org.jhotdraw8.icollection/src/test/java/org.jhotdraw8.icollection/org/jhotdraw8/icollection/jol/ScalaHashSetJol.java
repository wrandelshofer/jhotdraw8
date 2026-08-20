package org.jhotdraw8.icollection.jol;

import scala.collection.immutable.HashSet;

/// ScalaHashSetJol.
public class ScalaHashSetJol extends AbstractJol {
    public void main() {
        var test = new ScalaHashSetJol();
        int size = 1000;
        test.estimateMemoryUsage(size);
    }

    /// <pre>
    /// class scala.collection.immutable.HashSet with 1000 elements.
    /// total size              : 57104
    /// element size            : 24
    /// data size               : 24000 42%
    /// data structure size     : 33104 57%
    /// overhead per element    : 33.104 bytes
    /// ----footprint---
    /// scala.collection.immutable.HashSet@30b7c004d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        316        29      9440   [I
    ///        321        33     10808   [Ljava.lang.Object;
    ///       1000        24     24000   org.jhotdraw8.icollection.util.Key
    ///        321        40     12840   scala.collection.immutable.BitmapIndexedSetNode
    ///          1        16        16   scala.collection.immutable.HashSet
    ///       1959               57104   (total)
    /// </pre>

    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = generateSet(size, mask);
        var b = HashSet.<Key>newBuilder();
        for (var d : data) {
            b.addOne(d);
        }
        HashSet<Key> setA = b.result();
        Key head = setA.head();
        estimateMemoryUsage(setA, head, setA.size());
    }


}
