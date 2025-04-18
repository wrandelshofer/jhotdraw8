/*
 * @(#)CachingCollator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.text;

import org.jspecify.annotations.Nullable;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Comparator} that compares {@link String}s using a provided
 * {@link Collator}.
 * <p>
 * This comparator caches the collation keys.
 *
 */
public class CachingCollator implements Comparator<String> {

    private final Collator collator;
    private final Map<String, CollationKey> keyMap = new HashMap<>();

    public CachingCollator(Collator collator) {
        this.collator = collator;
    }

    @Override
    public int compare(@Nullable String o1, @Nullable String o2) {
        CollationKey k1 = keyMap.computeIfAbsent(o1 == null ? "" : o1, collator::getCollationKey);
        CollationKey k2 = keyMap.computeIfAbsent(o2 == null ? "" : o2, collator::getCollationKey);
        return k1.compareTo(k2);
    }

    public void clearCache() {
        keyMap.clear();
    }
}
