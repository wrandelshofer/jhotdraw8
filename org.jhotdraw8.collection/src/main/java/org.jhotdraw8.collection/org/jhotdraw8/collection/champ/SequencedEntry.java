/*
 * @(#)SequencedEntry.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Objects;

/**
 * A {@code SequencedEntry} stores an entry of a map and a sequence number.
 * <p>
 * {@code hashCode} and {@code equals} are based on the key and the value
 * of the entry - the sequence number is not included.
 */
class SequencedEntry<K, V> extends AbstractMap.SimpleImmutableEntry<K, V>
        implements SequencedData {
    private static final long serialVersionUID = 0L;
    private final int sequenceNumber;

    public SequencedEntry(@Nullable K key) {
        super(key, null);
        sequenceNumber = NO_SEQUENCE_NUMBER;
    }

    public SequencedEntry(@Nullable K key, @Nullable V value, int sequenceNumber) {
        super(key, value);
        this.sequenceNumber = sequenceNumber;
    }

    @NonNull
    static <K, V> SequencedEntry<K, V> update(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @NonNull
    static <K, V> SequencedEntry<K, V> updateAndMoveToFirst(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() + 1 ? oldK : newK;
    }

    @NonNull
    static <K, V> SequencedEntry<K, V> updateAndMoveToLast(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() - 1 ? oldK : newK;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    static <K, V> boolean keyEquals(@NonNull SequencedEntry<K, V> a, @NonNull SequencedEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    static <V, K> int keyHash(@NonNull SequencedEntry<K, V> a) {
        return Objects.hashCode(a.getKey());
    }
}
