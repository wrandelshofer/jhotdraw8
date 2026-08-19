
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champmap.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champmap.ChangeEvent;
import org.jhotdraw8.icollection.impl.IdentityObject;

/// This Builder allows to efficiently build a [PersistentHashMapWithNodeSubClasses] without
/// generating intermediate editions.
public class PersistentHashMapBuilderWithNodeSubClasses<K, V> implements MapBuilder<K, V, PersistentHashMapWithNodeSubClasses<K, V>> {
    private BitmapIndexedNode<K, V> hashMap = BitmapIndexedNode.emptyNode();
    private IdentityObject owner = new IdentityObject();
    private int size;

    public PersistentHashMapBuilderWithNodeSubClasses() {
    }

    @Override
    public PersistentHashMapBuilderWithNodeSubClasses<K, V> add(K key, V value) {
        var details = new ChangeEvent<V>();
        var newMap = hashMap.put(owner, key, value,
                PersistentHashMapWithNodeSubClasses.keyHash(key), 0, details,
                PersistentHashMapWithNodeSubClasses::keyHash);
        hashMap = newMap;
        size++;
        return this;
    }


    @Override
    public PersistentHashMapWithNodeSubClasses<K, V> build() {
        owner = new IdentityObject();
        return new PersistentHashMapWithNodeSubClasses<>(hashMap, size);
    }
}
