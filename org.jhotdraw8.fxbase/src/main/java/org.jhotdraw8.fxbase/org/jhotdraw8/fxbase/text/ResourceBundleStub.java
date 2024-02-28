/*
 * @(#)ResourceBundleStub.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.text;

import org.jhotdraw8.annotation.NonNull;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * This resource bundle can be used as a stub.
 */
public class ResourceBundleStub extends ResourceBundle {
    public @NonNull Enumeration<String> getKeys() {
        return null;
    }

    protected Object handleGetObject(@NonNull String key) {
        return key;
    }

    public String toString() {
        return "STUB_BUNDLE";
    }
};
