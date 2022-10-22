/*
 * @(#)ImmutableMapFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableMapTest;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ImmutableMapFacadeTest extends AbstractImmutableMapTest {
    @SuppressWarnings("unchecked")
    @Override
    protected <K, V> @NonNull ImmutableMapFacade<K, V> newInstance() {
        Function<Map<K, V>, Map<K, V>> cloneFunction = t -> (Map<K, V>) (Map<?, ?>) ((LinkedHashMap<K, V>) t).clone();
        return new ImmutableMapFacade<K, V>(new LinkedHashMap<K, V>(),
                cloneFunction);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected @NonNull <K, V> ImmutableMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return this.<K, V>newInstance().putAll(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return this.<K, V>newInstance().putAll(entries);
    }

}
