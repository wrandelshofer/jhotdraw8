/*
 * @(#)TypeToken.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcollection.typesafekey;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeToken captures the type of a generic class
 * including its type parameters.
 * <p>
 * Usage:
 * <pre>
 * {@literal TypeToken<List<Double>> tt = new TypeToken<List<Double>>{};}
 * {@literal Type type = tt.getType();}
 * </pre>
 *
 * @param <T> The type that this type token captures
 */
public class TypeToken<T> {
    private final Type runtimeType;

    public TypeToken() {
        this.runtimeType = capture();
    }

    /**
     * Returns the captured type.
     */
    final Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        boolean expression = superclass instanceof ParameterizedType;
        if (!expression) {
            throw new IllegalArgumentException(superclass + " isn't parameterized");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Returns the represented type.
     */
    public final Type getType() {
        return runtimeType;
    }
}
