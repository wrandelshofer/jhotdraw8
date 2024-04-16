/*
 * @(#)EmptyResources.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.resources;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class EmptyResources implements Resources {
    private @Nullable Resources parent;
    private final @NonNull ResourceBundle emptyResourceBundle;

    {
        try {
            emptyResourceBundle = new PropertyResourceBundle(new StringReader(""));
        } catch (IOException e) {
            throw new RuntimeException("Could not create an empty PropertyResourceBundle", e);
        }
    }

    public EmptyResources() {
    }

    @Override
    public @NonNull ResourceBundle asResourceBundle() {
        return emptyResourceBundle;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public @NonNull Class<?> getBaseClass() {
        return getClass();
    }

    @Override
    public @Nullable Object getModule() {
        return null;
    }

    @Override
    public @NonNull String getBaseName() {
        return "empty";
    }


    @Override
    public @NonNull Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public @NonNull String getString(String key) {
        throw new MissingResourceException("Could not find a resource with key=\"" + key + "\".",
                this.getClass().getName(),
                key);
    }

    @Override
    public @Nullable Resources getParent() {
        return parent;
    }

    @Override
    public @Nullable Object handleGetObjectRecursively(@NonNull String key) {
        return null;
    }

    @Override
    public @NonNull Enumeration<String> getKeys() {
        return Collections.emptyEnumeration();
    }

    @Override
    public void setParent(@Nullable Resources parent) {
        this.parent = parent;
    }
}
