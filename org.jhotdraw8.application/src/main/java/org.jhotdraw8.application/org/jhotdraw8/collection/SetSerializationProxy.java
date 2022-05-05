/*
 * @(#)MapSerializationProxy.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class SetSerializationProxy<E> implements Serializable {
    private final static long serialVersionUID = 0L;
    private transient Set<E> serialized;
    protected transient List<E> deserialized;

    protected SetSerializationProxy(Set<E> serialized) {
        this.serialized = serialized;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        s.writeInt(serialized.size());
        for (E e : serialized) {
            s.writeObject(e);
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int n = s.readInt();
        deserialized = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            deserialized.add(e);
        }
    }

    protected abstract Object readResolve();
}
