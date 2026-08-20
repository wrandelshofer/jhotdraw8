package org.jhotdraw8.icollection.impl.champset;

/// C# Enumerator for one-bits in an int.
///
/// Usage:
/// <pre>
/// int bitmask=...;
/// for (ForEachOneBit it=new ForEachOneBit(bitmask); it.moveNext(); ) {
///   int positionMask=it.currentPositionMask();
///   int index=it.currentIndex();
///   ...
/// }
/// </pre>
///
/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableSet/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class ForEachOneBit {
    private int mask;
    private int index = -1;
    private int bit;

    public ForEachOneBit(int mask) {
        this.mask = mask;
    }

    public boolean moveNext() {
        if (mask == 0) return false;
        bit = Integer.lowestOneBit(mask);
        index++;
        mask = mask ^ bit;
        return true;
    }

    public int currentPositionMask() {
        return bit;
    }

    public int currentIndex() {
        return index;
    }


}
