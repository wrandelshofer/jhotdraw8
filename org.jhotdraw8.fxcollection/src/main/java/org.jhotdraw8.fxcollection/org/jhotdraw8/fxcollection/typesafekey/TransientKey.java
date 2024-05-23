/*
 * @(#)TransientKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * TransientKey can be used to store temporary data in an object.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class TransientKey<T> extends AbstractKey<T> implements NullableKey<T> {


    public TransientKey(String name, Class<T> clazz) {
        super(name, clazz);
    }

    public TransientKey(String name, Class<T> clazz, T defaultValue) {
        super(name, clazz, defaultValue);
    }

    public TransientKey(@Nullable String name, Type clazz, boolean isNullable, boolean isTransient, @Nullable T defaultValue) {
        super(name, clazz, isNullable, isTransient, defaultValue);
    }


    @Override
    public boolean isTransient() {
        return true;
    }

}
