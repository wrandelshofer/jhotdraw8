package org.jhotdraw8.icollection.impl.champmap;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableMap/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class ModificationResult<K, V> {
    public TrieNode<K, V> node;
    public int sizeDelta;
    public @Nullable V oldValue;

    public ModificationResult(TrieNode<K, V> node, int sizeDelta, @Nullable V oldValue) {
        this.node = node;
        this.sizeDelta = sizeDelta;
        this.oldValue = oldValue;
    }

    ModificationResult<K, V> replaceNode(Function<TrieNode<K, V>, TrieNode<K, V>> operation) {
        node = operation.apply(node);
        return this;
    }
}
