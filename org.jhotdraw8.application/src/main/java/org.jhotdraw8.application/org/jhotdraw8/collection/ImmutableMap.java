/*
 * @(#)ImmutableMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

/**
 * Read-only interface for an immutable map; the implementation guarantees that
 * the state of the map does not change.
 *
 * @param <K> the key type of the map
 * @param <V> the value type of the map
 */
public interface ImmutableMap<K, V> extends ReadOnlyMap<K, V> {

}
