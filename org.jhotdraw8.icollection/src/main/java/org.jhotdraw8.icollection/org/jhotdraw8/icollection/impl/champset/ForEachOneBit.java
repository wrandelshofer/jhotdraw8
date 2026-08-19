package org.jhotdraw8.icollection.impl.champset;

/// Enumerator for one-bits in an int.
///
/// Usage:
/// <pre>
/// int value=...;
/// for (ForEachOneBit it=new ForEachOneBit(value); it.moveNext(); ) {
///   int positionMask=it.getPositionMask();
///   int newNodeIndex=it.getNewNodeIndex();
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

    public int getPositionMask() {
        return bit;
    }

    public int getNewNodeIndex() {
        return index;
    }


}
