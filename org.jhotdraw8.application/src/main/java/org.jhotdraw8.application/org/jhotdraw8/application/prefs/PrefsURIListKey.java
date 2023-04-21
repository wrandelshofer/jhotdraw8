/*
 * @(#)PrefsURIListKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.application.prefs;

import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * PrefsURIListKey. The words are separated by tab character.
 *
 * @author Werner Randelshofer
 */
public class PrefsURIListKey {
    private final String key;
    private final @NonNull List<String> defaultValue;


    public PrefsURIListKey(String key, @NonNull List<String> defaultValue) {
        this.key = key;
        this.defaultValue = Collections.unmodifiableList(new ArrayList<>(defaultValue));

    }

    public @NonNull List<String> get(Preferences prefs) {
        return defaultValue;
    }

    public void put(Preferences prefs, int newValue) {
    }
}
