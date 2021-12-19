/*
 * @(#)AbstractReadOnlyMap.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractReadOnlyMap<K, V> implements ReadOnlyMap<K, V> {
    public AbstractReadOnlyMap() {
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ReadOnlyMap)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ReadOnlyMap<K, V> m = (ReadOnlyMap<K, V>) o;
        if (m.size() != size()) {
            return false;
        }

        try {
            for (Map.Entry<K, V> e : readOnlyEntrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }


    public int hashCode() {
        int h = 0;
        for (Map.Entry<K, V> kvEntry : readOnlyEntrySet()) {
            h += kvEntry.hashCode();
        }
        return h;
    }

    final public String toString() {
        Iterator<Map.Entry<K, V>> i = entries();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }
}
