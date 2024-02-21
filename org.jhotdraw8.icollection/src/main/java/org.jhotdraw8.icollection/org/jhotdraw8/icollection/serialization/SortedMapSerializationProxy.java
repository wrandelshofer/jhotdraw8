/*
 * @(#)MapSerializationProxy.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.serialization;

import org.jhotdraw8.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * A serialization proxy that serializes a map independently of its internal
 * structure.
 * <p>
 * Usage:
 * <pre>
 * class MyMap&lt;K, V&gt; implements Map&lt;K, V&gt;, Serializable {
 *   private final static long serialVersionUID = 0L;
 *
 *   private Object writeReplace() throws ObjectStreamException {
 *      return new SerializationProxy&lt;&gt;(this);
 *   }
 *
 *   static class SerializationProxy&lt;K, V&gt;
 *                  extends SortedMapSerializationProxy&lt;K, V&gt; {
 *      private final static long serialVersionUID = 0L;
 *      SerializationProxy(Map&lt;K, V&gt; target) {
 *          super(target);
 *      }
 *     {@literal @Override}
 *      protected Object readResolve() {
 *          return new MyMap&lt;&gt;(deserializedComparator,deserializedEntries);
 *      }
 *   }
 * }
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Java Object Serialization Specification: 2 - Object Output Classes,
 *     2.5 The writeReplace Method</dt>
 *     <dd><a href="https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/output.html#the-writereplace-method"></a>oracle.com</dd>
 *
 *     <dt>Java Object Serialization Specification: 3 - Object Input Classes,
 *     3.7 The readResolve Method</dt>
 *     <dd><a href="https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/input.html#the-readresolve-method"></a>oracle.com</dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class SortedMapSerializationProxy<K, V> implements Serializable {
    private final transient SortedMap<K, V> serialized;
    protected transient List<Map.Entry<K, V>> deserializedEntries;
    protected transient Comparator<? super K> deserializedComparator;
    @Serial
    private static final long serialVersionUID = 0L;

    protected SortedMapSerializationProxy(SortedMap<K, V> serialized) {
        this.serialized = serialized;
    }

    @Serial
    private void writeObject(@NonNull ObjectOutputStream s)
            throws IOException {
        s.writeObject(serialized.comparator());
        s.writeInt(serialized.size());
        for (Map.Entry<K, V> entry : serialized.entrySet()) {
            s.writeObject(entry.getKey());
            s.writeObject(entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(@NonNull ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        deserializedComparator = (Comparator<? super K>) s.readObject();
        int n = s.readInt();
        deserializedEntries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
            V value = (V) s.readObject();
            deserializedEntries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
        }
    }

    @Serial
    protected abstract @NonNull Object readResolve();
}
