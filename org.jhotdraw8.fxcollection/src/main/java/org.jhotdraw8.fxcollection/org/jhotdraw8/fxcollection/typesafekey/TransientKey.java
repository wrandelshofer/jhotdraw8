/*
 * @(#)TransientKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * TransientKey can be used to store temporary data in an object.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class TransientKey<T> extends AbstractKey<T> implements NullableKey<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransientKey(@NonNull String name, @NonNull Class<T> clazz) {
        super(name, clazz);
    }

    public TransientKey(@NonNull String name, @NonNull Class<T> clazz, T defaultValue) {
        super(name, clazz, defaultValue);
    }

    public TransientKey(@Nullable String name, @NonNull Type clazz, boolean isNullable, boolean isTransient, @Nullable T defaultValue) {
        super(name, clazz, isNullable, isTransient, defaultValue);
    }


    @Override
    public boolean isTransient() {
        return true;
    }

}
