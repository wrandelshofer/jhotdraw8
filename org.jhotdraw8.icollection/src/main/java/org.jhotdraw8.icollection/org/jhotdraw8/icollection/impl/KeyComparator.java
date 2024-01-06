package org.jhotdraw8.icollection.impl;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Objects;

public class KeyComparator<K, V> implements Comparator<AbstractMap.SimpleImmutableEntry<K, V>> {
    private final @NonNull Comparator<K> comparator;

    public KeyComparator(@NonNull Comparator<K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(AbstractMap.SimpleImmutableEntry<K, V> o1, AbstractMap.SimpleImmutableEntry<K, V> o2) {
        return comparator.compare(o1.getKey(), o2.getKey());
    }

    public Comparator<K> getComparator() {
        return comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyComparator<?, ?> that = (KeyComparator<?, ?>) o;
        return Objects.equals(comparator, that.comparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparator);
    }
}
