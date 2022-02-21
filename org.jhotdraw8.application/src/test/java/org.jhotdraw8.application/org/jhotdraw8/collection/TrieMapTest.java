package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collections;
import java.util.Set;

public class TrieMapTest extends AbstractSetTest {
    @Override
    protected @NonNull <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return Collections.newSetFromMap(new TrieMap<>());
    }
}
