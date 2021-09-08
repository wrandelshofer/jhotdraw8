/*
 * @(#)ImmutableMap.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

/**
 * Provides query methods to a map. The state of the map does not change.
 *
 * @param <K> the key type of the map
 * @param <V> the value type of the map
 */
public interface ImmutableMap<K, V> extends ReadOnlyMap<K, V> {

}
