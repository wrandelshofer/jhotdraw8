package org.jhotdraw8.fxbase.text;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * This resource bundle can be used as a stub.
 */
public class ResourceBundleStub extends ResourceBundle {
    public Enumeration<String> getKeys() {
        return null;
    }

    protected Object handleGetObject(String key) {
        return key;
    }

    public String toString() {
        return "STUB_BUNDLE";
    }
};
