package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;

public class TrieListTest extends AbstractListTest {
    @Override
    protected @NonNull <T> List<T> create() {
        return new TrieList<>();
    }
}
