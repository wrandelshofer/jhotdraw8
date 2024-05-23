/*
 * @(#)ImmutableMapFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.AbstractImmutableMapTest;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ImmutableMapFacadeTest extends AbstractImmutableMapTest {
    @SuppressWarnings("unchecked")
    @Override
    protected <K, V> ImmutableMapFacade<K, V> newInstance() {
        Function<Map<K, V>, Map<K, V>> cloneFunction = t -> (Map<K, V>) ((LinkedHashMap<K, V>) t).clone();
        return new ImmutableMapFacade<>(new LinkedHashMap<>(),
                cloneFunction);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(Map<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(ReadOnlyMap<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> toClonedInstance(ImmutableMap<K, V> m) {
        return this.<K, V>newInstance().putAll(m);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return this.<K, V>newInstance().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
