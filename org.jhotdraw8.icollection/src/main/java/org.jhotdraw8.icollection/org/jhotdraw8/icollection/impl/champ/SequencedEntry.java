/*
 * @(#)SequencedEntry.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.Objects;

/**
 * A {@code SequencedEntry} stores an entry of a map and a sequence number.
 * <p>
 * {@code hashCode} and {@code equals} are based on the key and the value
 * of the entry - the sequence number is not included.
 */
public class SequencedEntry<K, V> extends AbstractMap.SimpleImmutableEntry<K, V>
        implements SequencedData {
    @Serial
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

    public static <K, V> boolean keyEquals(@NonNull SequencedEntry<K, V> a, @NonNull SequencedEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    public static <V, K> int entryKeyHash(@NonNull SequencedEntry<K, V> a) {
        return Objects.hashCode(a.getKey());
    }

    public static int keyHash(@NonNull Object a) {
        return Objects.hashCode(a);
    }

    @NonNull
    public static <K, V> SequencedEntry<K, V> update(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @NonNull
    public static <K, V> SequencedEntry<K, V> updateAndMoveToFirst(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.sequenceNumber == newK.sequenceNumber + 1
                ? oldK
                : oldK.sequenceNumber == newK.sequenceNumber + 1
                ? new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.sequenceNumber) : newK;
    }

    @NonNull
    public static <K, V> SequencedEntry<K, V> updateAndMoveToLast(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.sequenceNumber == newK.sequenceNumber - 1
                ? oldK
                : oldK.sequenceNumber == newK.sequenceNumber - 1
                ? new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.sequenceNumber) : newK;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
