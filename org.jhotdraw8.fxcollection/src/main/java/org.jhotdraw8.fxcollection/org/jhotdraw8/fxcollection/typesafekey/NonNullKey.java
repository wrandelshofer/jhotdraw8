/*
 * @(#)NonNullKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

public interface NonNullKey<@NonNull T> extends Key<@NonNull T>, NonNullMapAccessor<@NonNull T> {
}
