/*
 * @(#)MapSerializationProxy.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class MapSerializationProxy<K, V> implements Serializable {
    private transient Map<K, V> serialized;
    protected transient List<Map.Entry<K, V>> deserialized;
    private final static long serialVersionUID = 0L;

    protected MapSerializationProxy(Map<K, V> serialized) {
        this.serialized = serialized;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        s.writeInt(serialized.size());
        for (Map.Entry<K, V> entry : serialized.entrySet()) {
            s.writeObject(entry.getKey());
            s.writeObject(entry.getValue());
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int n = s.readInt();
        deserialized = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
            V value = (V) s.readObject();
            deserialized.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
        }
    }

    protected abstract Object readResolve();
}
