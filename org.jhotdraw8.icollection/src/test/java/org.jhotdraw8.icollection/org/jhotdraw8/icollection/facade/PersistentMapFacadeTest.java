/*
 * @(#)PersistentMapFacadeTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.AbstractPersistentMapTest;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class PersistentMapFacadeTest extends AbstractPersistentMapTest {
    @SuppressWarnings("unchecked")
    @Override
    protected <K, V> PersistentMapFacade<K, V> newInstance() {
        Function<Map<K, V>, Map<K, V>> cloneFunction = t -> (Map<K, V>) ((LinkedHashMap<K, V>) t).clone();
        return new PersistentMapFacade<>(new LinkedHashMap<>(),
                cloneFunction);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(Map<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(ReadableMap<K, V> map) {
        return this.<K, V>newInstance().putAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return this.<K, V>newInstance().putAll(m);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return this.<K, V>newInstance().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
