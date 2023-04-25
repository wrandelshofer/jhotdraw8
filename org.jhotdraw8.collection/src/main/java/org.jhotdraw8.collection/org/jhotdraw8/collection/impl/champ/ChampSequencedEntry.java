/*
 * @(#)SequencedEntry.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

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
public class ChampSequencedEntry<K, V> extends AbstractMap.SimpleImmutableEntry<K, V>
        implements ChampSequencedData {
    @Serial
    private static final long serialVersionUID = 0L;
    private final int sequenceNumber;

    public ChampSequencedEntry(@Nullable K key) {
        super(key, null);
        sequenceNumber = NO_SEQUENCE_NUMBER;
    }

    public ChampSequencedEntry(@Nullable K key, @Nullable V value, int sequenceNumber) {
        super(key, value);
        this.sequenceNumber = sequenceNumber;
    }

    public static <K, V> boolean keyEquals(@NonNull ChampSequencedEntry<K, V> a, @NonNull ChampSequencedEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    public static <V, K> int keyHash(@NonNull ChampSequencedEntry<K, V> a) {
        return Objects.hashCode(a.getKey());
    }

    @NonNull
    public static <K, V> ChampSequencedEntry<K, V> update(@NonNull ChampSequencedEntry<K, V> oldK, @NonNull ChampSequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new ChampSequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @NonNull
    public static <K, V> ChampSequencedEntry<K, V> updateAndMoveToFirst(@NonNull ChampSequencedEntry<K, V> oldK, @NonNull ChampSequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() + 1
                ? oldK
                : newK.getSequenceNumber() == oldK.getSequenceNumber() - 1
                ? new ChampSequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber()) : newK;
    }

    @NonNull
    public static <K, V> ChampSequencedEntry<K, V> updateAndMoveToLast(@NonNull ChampSequencedEntry<K, V> oldK, @NonNull ChampSequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() - 1
                ? oldK
                : newK.getSequenceNumber() == oldK.getSequenceNumber() + 1
                ? new ChampSequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber()) : newK;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
