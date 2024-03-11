/*
 * @(#)SetSerializationProxy.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.serialization;

import org.jhotdraw8.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * A serialization proxy that serializes a set independently of its internal
 * structure.
 * <p>
 * Usage:
 * <pre>
 * class MySet&lt;E&gt; implements SortedSet&lt;E&gt;, Serializable {
 *   private final static long serialVersionUID = 0L;
 *
 *   private Object writeReplace() throws ObjectStreamException {
 *      return new SerializationProxy&lt;&gt;(this);
 *   }
 *
 *   static class SerializationProxy&lt;E&gt;
 *                  extends SetSerializationProxy&lt;E&gt; {
 *      private final static long serialVersionUID = 0L;
 *      SerializationProxy(SortedSet&lt;E&gt; target) {
 *          super(target);
 *      }
 *     {@literal @Override}
 *      protected Object readResolve() {
 *          return new MySet&lt;&gt;(deserializedComparator,deserializedElements);
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
 * @param <E> the element type
 */
public abstract class SortedSetSerializationProxy<E> implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final transient SortedSet<E> serialized;
    protected transient List<E> deserializedElements;
    protected transient Comparator<E> deserializedComparator;

    protected SortedSetSerializationProxy(@NonNull SortedSet<E> serialized) {
        this.serialized = serialized;
    }

    @Serial
    private void writeObject(@NonNull ObjectOutputStream s)
            throws IOException {
        s.writeObject(serialized.comparator());
        s.writeInt(serialized.size());
        for (E e : serialized) {
            s.writeObject(e);
        }
    }

    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(@NonNull ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        deserializedComparator = (Comparator<E>) s.readObject();
        int n = s.readInt();
        deserializedElements = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            E e = (E) s.readObject();
            deserializedElements.add(e);
        }
    }

    @SuppressWarnings({"serial", "RedundantSuppression"})
// We define this abstract method here, because require that subclasses have this method.
    @Serial
    protected abstract @NonNull Object readResolve();
}
