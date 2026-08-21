package org.jhotdraw8.tests.icollection;

import io.vavr.collection.LinkedHashSet;


public class VavrLinkedHashSetJol extends AbstractJol {
    void main() {
        int size = 1000;
        estimateMemoryUsage(size);
    }

    /// <pre>
    /// class io.vavr.collection.LinkedHashSet with 1000 elements.
    /// total size              : 116008
    /// element size            : 24
    /// data size               : 24000 20%
    /// data structure size     : 92008 79%
    /// overhead per element    : 92.008 bytes
    /// ----footprint---
    /// io.vavr.collection.LinkedHashSet@c81cdd1d footprint:
    ///      COUNT       AVG       SUM   DESCRIPTION
    ///        351        32     11488   [Ljava.lang.Object;
    ///       1000        24     24000   io.vavr.Tuple2
    ///          1        24        24   io.vavr.collection.HashArrayMappedTrieModule$ArrayNode
    ///        350        24      8400   io.vavr.collection.HashArrayMappedTrieModule$IndexedNode
    ///       1000        24     24000   io.vavr.collection.HashArrayMappedTrieModule$LeafSingleton
    ///          1        16        16   io.vavr.collection.HashMap
    ///          1        24        24   io.vavr.collection.LinkedHashMap
    ///          1        16        16   io.vavr.collection.LinkedHashSet
    ///       1000        24     24000   io.vavr.collection.List$Cons
    ///          1        16        16   io.vavr.collection.List$Nil
    ///          1        24        24   io.vavr.collection.Queue
    ///       1000        24     24000   org.jhotdraw8.tests.icollection.Key
    ///       4707              116008   (total)
    /// </pre>
    public void estimateMemoryUsage(int size) {
        final int mask = ~64;
        var data = AbstractJol.generateSet(size, mask);
        var setA = LinkedHashSet.<Key>empty();
        for (var d : data) {
            setA = setA.add(d);
        }
        Key head = setA.head();
        AbstractJol.estimateMemoryUsage(setA, head, setA.size());
    }


}
