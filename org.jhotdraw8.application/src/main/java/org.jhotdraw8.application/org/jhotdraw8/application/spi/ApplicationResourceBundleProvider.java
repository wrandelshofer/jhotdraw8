/*
 * @(#)ApplicationResourceBundleProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.spi;

import org.jhotdraw8.annotation.NonNull;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.AbstractResourceBundleProvider;

public class ApplicationResourceBundleProvider extends AbstractResourceBundleProvider {

    public ApplicationResourceBundleProvider() {
    }

    @Override
    public ResourceBundle getBundle(@NonNull String baseName, @NonNull Locale locale) {
        return ResourceBundle.getBundle(baseName, locale);
    }
}
