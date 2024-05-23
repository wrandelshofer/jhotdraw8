/*
 * @(#)DrawResourceBundleProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.spi;


import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.AbstractResourceBundleProvider;

public class DrawResourceBundleProvider extends AbstractResourceBundleProvider {

    public DrawResourceBundleProvider() {
    }

    @Override
    public ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName, locale);
    }
}
