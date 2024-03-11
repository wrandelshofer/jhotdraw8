/*
 * @(#)NonNullObjectKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.Type;

/**
 * A simple {@link Key} which has a non-nullable value.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class NonNullObjectKey<@NonNull T> extends AbstractKey<@NonNull T> implements
        NonNullKey<@NonNull T> {


    /**
     * Creates a new instance with the specified name, type token class, default
     * value.
     *
     * @param name         The name of the name.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     */
    public NonNullObjectKey(@NonNull String name, @NonNull Type type, @NonNull T defaultValue) {
        super(name, type, false, false, defaultValue);
    }
}
