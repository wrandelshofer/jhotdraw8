package org.jhotdraw8.icollection.impl.champlinked;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

/// This code has been derived from
/// [kotlix.collections.immutable, PersistentHashMapBuilder.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/024f04c89ad357da4acefca449d633645ce113ff/core/commonMain/src/implementations/immutableMap/PersistentHashMapBuilder.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class TrieBuilder<K, V> {

    public MutabilityOwnership ownership = new MutabilityOwnership();

    public TrieBuilder() {
    }

    public TrieBuilder(MutabilityOwnership ownership) {
        this.ownership = ownership;
    }

    public int modCount;
    public int size;
    public @Nullable V value;
    public @Nullable Object @Nullable [] entry;

    public V getAndClearOperationResult() {
        var result = value;
        value = null;
        return result;
    }

    public boolean isModified() {
        return modCount != 0 || size != 0;
    }

    public TrieBuilder<K, V> reset() {
        modCount = 0;
        size = 0;
        value = null;
        return this;
    }
}
