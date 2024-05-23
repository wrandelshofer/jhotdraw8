/*
 * @(#)ResourceBundleStub.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.text;


import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * This resource bundle can be used as a stub.
 */
public class ResourceBundleStub extends ResourceBundle {
    public Enumeration<String> getKeys() {
        return Collections.emptyEnumeration();
    }

    protected Object handleGetObject(String key) {
        return key;
    }

    public String toString() {
        return "STUB_BUNDLE";
    }
}
