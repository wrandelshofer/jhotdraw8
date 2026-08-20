
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.champmap.DeltaCounter;
import org.jhotdraw8.icollection.impl.champmap.TrieBuilder;
import org.jhotdraw8.icollection.impl.champmap.TrieNode;

import java.util.Map;
import java.util.Objects;

/// This Builder allows to efficiently build a [PersistentHashMap] without
/// generating intermediate editions.
public class PersistentHashMapBuilder<K, V> implements MapBuilder<K, V, PersistentHashMap<K, V>> {
    private TrieNode<K, V> hashMap = TrieNode.empty();
    private TrieBuilder<K, V> builder = new TrieBuilder<>();

    public PersistentHashMapBuilder() {
    }

    @Override
    public PersistentHashMapBuilder<K, V> put(K key, V value) {
        hashMap = hashMap.mutablePut(
                Objects.hashCode(key), key, value, 0, builder);
        return this;
    }

    /// Puts all entries from the specified map.
    @SuppressWarnings("unchecked")
    public PersistentHashMapBuilder<K, V> putEntries(Iterable<? extends Map.Entry<? extends K, ? extends V>> map) {
        if (map instanceof PersistentHashMap<?, ?> phm) {
            var deltaCounter = new DeltaCounter();
            int oldSize = builder.size;
            hashMap = hashMap.mutablePutAll((TrieNode<K, V>) phm.node, 0, deltaCounter, builder);
            builder.size = oldSize + phm.size - deltaCounter.count;
        }
        for (Map.Entry<? extends K, ? extends V> e : map) {
            put(e.getKey(), e.getValue());
        }
        return this;
    }

    @Override
    public PersistentHashMapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
        if (map instanceof MutableHashMap<? extends K, ? extends V> phm) {
            return putEntries(phm.toPersistent());
        }
        return (PersistentHashMapBuilder<K, V>) MapBuilder.super.putAll(map);
    }

    @Override
    public PersistentHashMap<K, V> build() {
        builder.ownership = new MutabilityOwnership();
        return new PersistentHashMap<>(hashMap, builder.size);
    }
}
