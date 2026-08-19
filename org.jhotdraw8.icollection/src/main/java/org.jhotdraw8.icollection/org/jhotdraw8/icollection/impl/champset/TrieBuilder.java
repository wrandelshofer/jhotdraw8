package org.jhotdraw8.icollection.impl.champset;

import org.jhotdraw8.icollection.impl.IdentityObject;

/// This code has been derived from
/// [kotlix.collections.immutable, TrieNode.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/core/commonMain/src/implementations/immutableSet/TrieNode.kt),
/// JetBrains s.r.o.
/// [Apache License 2.0](https://github.com/Kotlin/kotlinx.collections.immutable/blob/578f6ed44cbafdb16bef330d1ec4a6b753201516/LICENSE.txt)
public class TrieBuilder<E> {
    public int size;
    public IdentityObject ownership = new IdentityObject();

    public TrieBuilder<E> reset() {
        size = 0;
        return this;
    }

    public boolean isModified() {
        return size != 0;
    }

    public TrieBuilder() {
        this(new IdentityObject());
    }

    public TrieBuilder(IdentityObject ownership) {
        this.ownership = ownership;
    }
}
