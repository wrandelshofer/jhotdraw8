package org.jhotdraw8.tests.icollection;

import io.vavr.collection.TreeSet;


public class VavrTreeSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class io.vavr.collection.TreeSet with 1000 elements.
    /// total size              : 64192
    /// element size            : 24
    /// data size               : 24000 37%
    /// data structure size     : 40192 62%
    /// overhead per element    : 40.192 bytes
    /// ----footprint---
    /// io.vavr.collection.TreeSet@e720b71d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///          2        24        48   [B
    ///          1        16        16   io.vavr.collection.NaturalComparator
    ///          2        24        48   io.vavr.collection.RedBlackTree$Color
    ///          1        16        16   io.vavr.collection.RedBlackTreeModule$Empty
    ///       1000        40     40000   io.vavr.collection.RedBlackTreeModule$Node
    ///          1        16        16   io.vavr.collection.TreeSet
    ///          2        24        48   java.lang.String
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       2009               64192   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateSet(size, mask);
        var setA = TreeSet.<Key>empty();
        for (var d : data) {
            setA = setA.add(d);
        }
        Key head = setA.head();
        AbstractJol.estimateMemoryUsage(setA, head, setA.size());
    }


}
