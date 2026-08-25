package org.jhotdraw8.tests.icollection;


import scala.collection.immutable.TreeSet;
import scala.math.Ordering;

public class ScalaTreeSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// ```
    /// class scala.collection.immutable.TreeSet with 1000 elements.
    /// total size              : 56072
    /// element size            : 24
    /// data size               : 24000 42%
    /// data structure size     : 32072 57%
    /// overhead per element    : 32.072 bytes
    /// ----footprint---
    /// scala.collection.immutable.TreeSet@76329302d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///          1        16        16   org.jhotdraw8.tests.icollection.ScalaTreeSetJol
    ///          1        16        16   org.jhotdraw8.tests.icollection.ScalaTreeSetJol$1
    ///       1000        32     32000   scala.collection.immutable.RedBlackTree$Tree
    ///          1        24        24   scala.collection.immutable.TreeSet
    ///          1        16        16   scala.runtime.BoxedUnit
    ///       2004               56072   (total)
    /// ```
    public void estimateMemoryUsage(int size) {
        final int mask = -1;//~64;
        var data = AbstractJol.generateSet(size, mask);
        var b = TreeSet.<Key>newBuilder(new Ordering<Key>() {
            @Override
            public int compare(Key x, Key y) {
                return y.value - x.value;
            }

        });
        for (var d : data) {
            b.addOne(d);
        }
        TreeSet<Key> setA = b.result();
        Key head = setA.head();
        AbstractJol.estimateMemoryUsage(setA, head, setA.size());
    }


}
